const {
  DELETE_SUCCESS_MESSAGE,
  DELETE_SUCCESS_STATUS,
  PHOTO_DELETE_SUCCESS_MESSAGE,
} = require('./constants');

function sendSuccess(res, data, status = 200) {
  res.status(status).json({ data });
}

function sendError(res, status, code, message) {
  res.status(status).json({
    error: {
      code,
      message,
    },
  });
}

function sendDeleteSuccess(res) {
  res.status(200).json({
    status: DELETE_SUCCESS_STATUS,
    message: DELETE_SUCCESS_MESSAGE,
  });
}

function sendPhotoDeleteSuccess(res, photoId) {
  res.status(200).json({
    status: DELETE_SUCCESS_STATUS,
    message: PHOTO_DELETE_SUCCESS_MESSAGE,
    photoId,
  });
}

module.exports = {
  sendSuccess,
  sendError,
  sendDeleteSuccess,
  sendPhotoDeleteSuccess,
};
