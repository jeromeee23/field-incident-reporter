const fs = require('fs');
const fsp = require('fs/promises');
const path = require('path');
const {
  PHOTO_EXTENSIONS,
  PHOTO_FILENAME_REGEX,
  UUID_REGEX,
} = require('./constants');

const UPLOADS_DIR = path.resolve(__dirname, '..', 'uploads');

function ensureUploadsDir() {
  fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

function isInsideUploadsDir(filePath) {
  const resolved = path.resolve(filePath);
  return resolved.startsWith(UPLOADS_DIR + path.sep);
}

function mimeTypeForExtension(extension) {
  switch (extension.toLowerCase()) {
    case 'jpg':
    case 'jpeg':
      return 'image/jpeg';
    case 'png':
      return 'image/png';
    case 'webp':
      return 'image/webp';
    default:
      return 'application/octet-stream';
  }
}

function buildPublicUrl(req, filename) {
  const configured = process.env.PUBLIC_BASE_URL;
  const base = configured
    ? configured.replace(/\/$/, '')
    : `${req.protocol}://${req.get('host')}`;
  return `${base}/uploads/${filename}`;
}

function resolveServedFile(filename) {
  if (typeof filename !== 'string' || !PHOTO_FILENAME_REGEX.test(filename)) {
    return null;
  }

  const filePath = path.resolve(UPLOADS_DIR, filename);
  if (!isInsideUploadsDir(filePath)) {
    return null;
  }

  return filePath;
}

async function rollbackWrittenFiles(filePaths) {
  const results = await Promise.allSettled(
    filePaths.map((filePath) => fsp.unlink(filePath)),
  );

  results.forEach((result) => {
    if (result.status === 'rejected' && result.reason && result.reason.code !== 'ENOENT') {
      console.error('Failed to roll back an uploaded photo file.', result.reason);
    }
  });
}

async function writeValidatedPhotos(validatedFiles) {
  const written = [];
  try {
    for (const file of validatedFiles) {
      const filePath = path.resolve(UPLOADS_DIR, file.filename);
      if (!isInsideUploadsDir(filePath)) {
        throw new Error('Refusing to write a photo outside the uploads directory.');
      }
      await fsp.writeFile(filePath, file.buffer, { flag: 'wx' });
      written.push(filePath);
    }
    return written;
  } catch (error) {
    await rollbackWrittenFiles(written);
    throw error;
  }
}

async function findPhotoFile(photoId) {
  if (typeof photoId !== 'string' || !UUID_REGEX.test(photoId)) {
    return null;
  }

  for (const extension of PHOTO_EXTENSIONS) {
    const filePath = path.resolve(UPLOADS_DIR, `${photoId}.${extension}`);
    if (!isInsideUploadsDir(filePath)) {
      return null;
    }

    try {
      await fsp.access(filePath);
      return filePath;
    } catch (error) {
      if (error.code === 'ENOENT') {
        continue;
      }
      throw error;
    }
  }

  return null;
}

async function deletePhotoFile(photoId) {
  const filePath = await findPhotoFile(photoId);
  if (!filePath) {
    return false;
  }

  await fsp.unlink(filePath);
  return true;
}

async function deleteLocalPhotosForIncident(photos) {
  if (!Array.isArray(photos)) {
    return;
  }

  for (const photo of photos) {
    if (!photo || typeof photo.id !== 'string' || !UUID_REGEX.test(photo.id)) {
      continue;
    }

    const filePath = await findPhotoFile(photo.id);
    if (!filePath) {
      continue;
    }

    try {
      await fsp.unlink(filePath);
    } catch (error) {
      if (error.code === 'ENOENT') {
        continue;
      }
      throw error;
    }
  }
}

module.exports = {
  UPLOADS_DIR,
  ensureUploadsDir,
  mimeTypeForExtension,
  buildPublicUrl,
  resolveServedFile,
  writeValidatedPhotos,
  findPhotoFile,
  deletePhotoFile,
  deleteLocalPhotosForIncident,
};
