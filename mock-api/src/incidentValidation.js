const {
  CATEGORIES,
  CREATE_ALLOWED_FIELDS,
  DESCRIPTION_MAX_LENGTH,
  DESCRIPTION_MIN_LENGTH,
  ERROR_CODES,
  LOCATION_MAX_LENGTH,
  PHOTO_ALLOWED_FIELDS,
  PHOTOS_MAX_COUNT,
  STATUSES,
  STATUS_ALLOWED_FIELDS,
  TITLE_MAX_LENGTH,
  TITLE_MIN_LENGTH,
  UPDATE_ALLOWED_FIELDS,
  UUID_REGEX,
} = require('./constants');

function invalidRequest(message) {
  return {
    status: 400,
    code: ERROR_CODES.INVALID_REQUEST,
    message,
  };
}

function validationError(message) {
  return {
    status: 400,
    code: ERROR_CODES.VALIDATION_ERROR,
    message,
  };
}

function isPlainObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function findUnexpectedFields(body, allowedFields) {
  return Object.keys(body).filter((key) => !allowedFields.includes(key));
}

function assertAllowedFields(body, allowedFields) {
  if (!isPlainObject(body)) {
    return invalidRequest('Request body must be a JSON object.');
  }

  const unexpected = findUnexpectedFields(body, allowedFields);
  if (unexpected.length > 0) {
    return invalidRequest(
      `Request contains unexpected fields: ${unexpected.join(', ')}.`,
    );
  }

  return null;
}

function parseStatusFilter(statusQuery) {
  if (statusQuery === undefined || statusQuery === null) {
    return { statuses: null };
  }

  if (Array.isArray(statusQuery)) {
    return {
      error: validationError('Status filter must be a single comma-separated value.'),
    };
  }

  if (typeof statusQuery !== 'string') {
    return { error: validationError('Status filter is invalid.') };
  }

  const trimmed = statusQuery.trim();
  if (trimmed === '') {
    return { statuses: null };
  }

  const tokens = trimmed.split(',').map((token) => token.trim());
  const statuses = [];

  for (const token of tokens) {
    if (token === '' || !STATUSES.includes(token)) {
      return {
        error: validationError(
          token === ''
            ? 'Status filter contains an empty value.'
            : `Invalid status value: ${token}.`,
        ),
      };
    }
    if (!statuses.includes(token)) {
      statuses.push(token);
    }
  }

  return { statuses };
}

function validatePhotos(photos) {
  if (!Array.isArray(photos)) {
    return { error: validationError('Photos must be an array.') };
  }

  if (photos.length > PHOTOS_MAX_COUNT) {
    return {
      error: validationError(`A maximum of ${PHOTOS_MAX_COUNT} photos is allowed.`),
    };
  }

  const sanitized = [];
  for (let index = 0; index < photos.length; index += 1) {
    const photo = photos[index];
    if (!isPlainObject(photo)) {
      return {
        error: validationError(`Photo at index ${index} must be an object.`),
      };
    }

    const unexpected = findUnexpectedFields(photo, PHOTO_ALLOWED_FIELDS);
    if (unexpected.length > 0) {
      return {
        error: invalidRequest(
          `Photo at index ${index} contains unexpected fields: ${unexpected.join(', ')}.`,
        ),
      };
    }

    if (typeof photo.id !== 'string' || photo.id.trim() === '') {
      return {
        error: validationError(`Photo at index ${index} must have a non-empty id.`),
      };
    }

    if (typeof photo.url !== 'string' || photo.url.trim() === '') {
      return {
        error: validationError(`Photo at index ${index} must have a non-empty url.`),
      };
    }

    sanitized.push({
      id: photo.id.trim(),
      url: photo.url.trim(),
    });
  }

  return { value: sanitized };
}

function validateSharedFields(body) {
  if (typeof body.title !== 'string') {
    return { error: validationError('Title is required.') };
  }
  const title = body.title.trim();
  if (title.length < TITLE_MIN_LENGTH || title.length > TITLE_MAX_LENGTH) {
    return {
      error: validationError(
        `Title must be between ${TITLE_MIN_LENGTH} and ${TITLE_MAX_LENGTH} characters.`,
      ),
    };
  }

  if (typeof body.description !== 'string') {
    return { error: validationError('Description is required.') };
  }
  const description = body.description.trim();
  if (
    description.length < DESCRIPTION_MIN_LENGTH
    || description.length > DESCRIPTION_MAX_LENGTH
  ) {
    return {
      error: validationError(
        `Description must be between ${DESCRIPTION_MIN_LENGTH} and ${DESCRIPTION_MAX_LENGTH} characters.`,
      ),
    };
  }

  if (typeof body.category !== 'string' || !CATEGORIES.includes(body.category)) {
    return { error: validationError('Category is invalid.') };
  }

  let otherCategoryDescription = body.otherCategoryDescription;
  if (body.category === 'OTHER') {
    if (typeof otherCategoryDescription !== 'string') {
      return {
        error: validationError('otherCategoryDescription is required for OTHER category.'),
      };
    }
    otherCategoryDescription = otherCategoryDescription.trim();
    if (otherCategoryDescription === '') {
      return {
        error: validationError('otherCategoryDescription is required for OTHER category.'),
      };
    }
  } else if (otherCategoryDescription !== null) {
    return {
      error: validationError('otherCategoryDescription must be null unless category is OTHER.'),
    };
  }

  if (typeof body.location !== 'string') {
    return { error: validationError('Location is required.') };
  }
  const location = body.location.trim();
  if (location.length === 0 || location.length > LOCATION_MAX_LENGTH) {
    return {
      error: validationError(
        `Location is required and must be at most ${LOCATION_MAX_LENGTH} characters.`,
      ),
    };
  }

  const photosResult = validatePhotos(body.photos);
  if (photosResult.error) {
    return photosResult;
  }

  return {
    value: {
      title,
      description,
      category: body.category,
      otherCategoryDescription,
      location,
      photos: photosResult.value,
    },
  };
}

function validateCreateBody(body) {
  const fieldError = assertAllowedFields(body, CREATE_ALLOWED_FIELDS);
  if (fieldError) {
    return { error: fieldError };
  }

  if (typeof body.id !== 'string' || !UUID_REGEX.test(body.id)) {
    return { error: validationError('Incident id must be a valid UUID.') };
  }

  const shared = validateSharedFields(body);
  if (shared.error) {
    return shared;
  }

  return {
    value: {
      id: body.id,
      ...shared.value,
    },
  };
}

function validateUpdateBody(body) {
  const fieldError = assertAllowedFields(body, UPDATE_ALLOWED_FIELDS);
  if (fieldError) {
    return { error: fieldError };
  }

  return validateSharedFields(body);
}

function validateStatusBody(body) {
  const fieldError = assertAllowedFields(body, STATUS_ALLOWED_FIELDS);
  if (fieldError) {
    return { error: fieldError };
  }

  if (typeof body.status !== 'string') {
    return { error: validationError('Status is required.') };
  }

  if (!STATUSES.includes(body.status)) {
    return { error: validationError('Status is invalid.') };
  }

  return { value: { status: body.status } };
}

module.exports = {
  parseStatusFilter,
  validateCreateBody,
  validateUpdateBody,
  validateStatusBody,
};
