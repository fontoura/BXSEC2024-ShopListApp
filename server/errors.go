package main

import (
	"errors"
	"fmt"
)

// HttpError is a custom error type that includes an HTTP error code, a message, and an optional root cause.
type HttpError struct {
	HttpErrorCode int
	Message       string
	RootCause     error
}

// Error implements the error interface for HttpError.
func (e *HttpError) Error() string {
	if e.RootCause != nil {
		return fmt.Sprintf("HTTP %d: %s - root cause: %v", e.HttpErrorCode, e.Message, e.RootCause)
	}
	return fmt.Sprintf("HTTP %d: %s", e.HttpErrorCode, e.Message)
}

// NewHttpError creates a new HttpError with the given code, message, and optional root cause.
func NewHttpError(code int, message string, rootCause error) *HttpError {
	return &HttpError{
		HttpErrorCode: code,
		Message:       message,
		RootCause:     rootCause,
	}
}

// AsHttpError takes an error and casts it to an HttpError.
// If the error is not an HttpError, it wraps it in a 500 HttpError.
func AsHttpError(err error, fallbackMessage string) *HttpError {
	var httpErr *HttpError
	if errors.As(err, &httpErr) {
		return httpErr
	}
	return NewInternalServerError(fallbackMessage, err)
}

func NewBadRequestError(message string, rootCause error) *HttpError {
	return NewHttpError(400, message, rootCause)
}

func NewUnauthorizedError(message string, rootCause error) *HttpError {
	return NewHttpError(401, message, rootCause)
}

func NewNotFoundError(message string, rootCause error) *HttpError {
	return NewHttpError(404, message, rootCause)
}

func NewConflictError(message string, rootCause error) *HttpError {
	return NewHttpError(409, message, rootCause)
}

func NewInternalServerError(message string, rootCause error) *HttpError {
	return NewHttpError(500, message, rootCause)
}
