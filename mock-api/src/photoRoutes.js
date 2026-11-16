const crypto = require('crypto');
const fsp = require('fs/promises');
const path = require('path');
const express = require('express');
const multer = require('multer');
const {
  ERROR_CODES,
  PHOTO_MAX_SIZE_BYTES,
  PHOTOS_MAX_COUNT,
  UUID_REGEX,
} = require('./constants');
const { sendError, sendPhotoDeleteSuccess, sendSuccess } = require('./http');
const { detectImageFormat } = require('./imageValidation');
const {
  buildPublicUrl,
  deletePhotoFile,
  mimeTypeForExtension,
  resolveServedFile,
  writeValidatedPhotos,
} = require('./photoStorage');

const router = express.Router();

const uploadPhotos = multer({
  storage: multer.memoryStorage(),
  limits: {
    files: PHOTOS_MAX_COUNT,
    fileSize: PHOTO_MAX_SIZE_BYTES,
    fields: 1,
  },
}).array('photos', PHOTOS_MAX_COUNT);

function mapMulterError(err) {
  switch (err.code) {
    case 'LIMIT_FILE_SIZE':
      return {
        status: 400,
        code: ERROR_CODES.VALIDATION_ERROR,
        message: 'Each photo must be 5 MB or smaller.',
      };
    case 'LIMIT_FILE_COUNT':
      return {
        status: 400,
        code: ERROR_CODES.VALIDATION_ERROR,
        message: `A maximum of ${PHOTOS_MAX_COUNT} photos is allowed.`,
      };
    case 'LIMIT_UNEXPECTED_FILE':
      if (err.field === 'photos') {
        return {
          status: 400,
          code: ERROR_CODES.VALIDATION_ERROR,
          message: `A maximum of ${PHOTOS_MAX_COUNT} photos is allowed.`,
        };
      }
      return {
        status: 400,
        code: ERROR_CODES.INVALID_REQUEST,
        message: err.field
          ? `Request contains unexpected fields: ${err.field}.`
          : 'Request contains unexpected fields.',
      };
    case 'LIMIT_FIELD_COUNT':
      return {
        status: 400,
        code: ERROR_CODES.INVALID_REQUEST,
        message: 'Request contains unexpected fields.',
      };
    default:
      return {
        status: 400,
        code: ERROR_CODES.INVALID_REQUEST,
        message: 'Invalid multipart upload.',
      };
  }
}

function parsePhotoUpload(req, res, next) {
  const contentType = req.headers['content-type'] || '';
  if (!contentType.toLowerCase().includes('multipart/form-data')) {
    return sendError(
      res,
      400,
      ERROR_CODES.INVALID_REQUEST,
      'Content-Type must be multipart/form-data.',
    );
  }

  uploadPhotos(req, res, (err) => {
    if (!err) {
      next();
      return;
    }

    if (err instanceof multer.MulterError) {
      const mapped = mapMulterError(err);
      return sendError(res, mapped.status, mapped.code, mapped.message);
    }

    return next(err);
  });
}

router.post('/photos', parsePhotoUpload, async (req, res, next) => {
  try {
    const body = req.body || {};
    const unexpected = Object.keys(body).filter((key) => key !== 'incidentId');
    if (unexpected.length > 0) {
      return sendError(
        res,
        400,
        ERROR_CODES.INVALID_REQUEST,
        `Request contains unexpected fields: ${unexpected.join(', ')}.`,
      );
    }

    if (typeof body.incidentId !== 'string' || !UUID_REGEX.test(body.incidentId.trim())) {
      return sendError(
        res,
        400,
        ERROR_CODES.VALIDATION_ERROR,
        'Incident id must be a valid UUID.',
      );
    }

    const files = req.files || [];
    if (files.length === 0) {
      return sendError(
        res,
        400,
        ERROR_CODES.VALIDATION_ERROR,
        'At least one photo is required.',
      );
    }

    const validated = [];
    for (let index = 0; index < files.length; index += 1) {
      const file = files[index];
      if (!file.buffer || file.buffer.length === 0) {
        return sendError(
          res,
          400,
          ERROR_CODES.VALIDATION_ERROR,
          'Each photo must be a non-empty image file.',
        );
      }

      const format = detectImageFormat(file.buffer);
      if (!format) {
        return sendError(
          res,
          400,
          ERROR_CODES.VALIDATION_ERROR,
          'Only JPEG, PNG, and WebP photos are allowed.',
        );
      }

      const id = crypto.randomUUID();
      validated.push({
        id,
        filename: `${id}.${format.extension}`,
        buffer: file.buffer,
      });
    }

    await writeValidatedPhotos(validated);

    const photos = validated.map((file) => ({
      id: file.id,
      url: buildPublicUrl(req, file.filename),
    }));

    return sendSuccess(res, { photos }, 201);
  } catch (error) {
    return next(error);
  }
});

router.delete('/photos/:photoId', async (req, res, next) => {
  const photoId = req.params.photoId;
  if (!UUID_REGEX.test(photoId)) {
    return sendError(
      res,
      400,
      ERROR_CODES.VALIDATION_ERROR,
      'Photo id must be a valid UUID.',
    );
  }

  try {
    const deleted = await deletePhotoFile(photoId);
    if (!deleted) {
      return sendError(
        res,
        404,
        ERROR_CODES.PHOTO_NOT_FOUND,
        'Photo not found.',
      );
    }

    return sendPhotoDeleteSuccess(res, photoId);
  } catch (error) {
    return next(error);
  }
});

router.get('/:filename', async (req, res, next) => {
  const filePath = resolveServedFile(req.params.filename);
  if (!filePath) {
    return sendError(
      res,
      404,
      ERROR_CODES.PHOTO_NOT_FOUND,
      'Photo not found.',
    );
  }

  try {
    await fsp.access(filePath);
  } catch (error) {
    if (error.code === 'ENOENT') {
      return sendError(
        res,
        404,
        ERROR_CODES.PHOTO_NOT_FOUND,
        'Photo not found.',
      );
    }
    return next(error);
  }

  const extension = path.extname(filePath).slice(1);
  res.type(mimeTypeForExtension(extension));
  return res.sendFile(filePath, (error) => {
    if (!error) {
      return;
    }
    if (res.headersSent) {
      return next(error);
    }
    if (error.code === 'ENOENT') {
      return sendError(
        res,
        404,
        ERROR_CODES.PHOTO_NOT_FOUND,
        'Photo not found.',
      );
    }
    return next(error);
  });
});

module.exports = router;
