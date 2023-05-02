use std::io::{Error, ErrorKind};
use reqwest;
use jni;
use std::time::{SystemTime, UNIX_EPOCH};
use serde::{Deserialize, Serialize};
use serde_json;
use minreq;

#[derive(Serialize, Deserialize, Copy, Clone, Debug)]
pub enum ErrorType {
    Error, // default-error
    JNIError, //occurs when using JNI
    INPUTError, //occurs when input is invalid (e.g. invalid hostname...)
    NOTFOUNDError, //occurs when a resource is not found
    COMMUNICATIONError //occurs on UDP-error, POST-request error (host unreachable)...
}

#[derive(Serialize, Deserialize, Debug)]
pub struct CruspError {
    error_type: ErrorType,
    pub msg: String,
    pub time: u128,
}

impl CruspError {
    pub fn new_communication(message: String) -> Self {
        CruspError {
            error_type: ErrorType::COMMUNICATIONError,
            msg: message,
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }

    pub fn new_error(message: String) -> Self {
        CruspError {
            error_type: ErrorType::Error,
            msg: message,
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }

    pub fn new_input(message: String) -> Self {
        CruspError {
            error_type: ErrorType::INPUTError,
            msg: message,
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }

    pub fn new_not_found(message: String) -> Self {
        CruspError {
            error_type: ErrorType::NOTFOUNDError,
            msg: message,
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }

    pub fn get_error_type(&self) -> ErrorType {
        self.error_type
    }
}

impl From<serde_json::error::Error> for CruspError {
    fn from(err: serde_json::error::Error) -> Self {
        CruspError {
            error_type: ErrorType::COMMUNICATIONError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<Error> for CruspError {
    fn from(err: Error) -> Self {
        let error_type = match err.kind(){
            ErrorKind::PermissionDenied => ErrorType::COMMUNICATIONError,
            _ => ErrorType::COMMUNICATIONError,
        };

        CruspError{
            error_type,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<reqwest::Error> for CruspError {
    fn from (err: reqwest::Error) -> Self {
        CruspError {
            error_type: ErrorType::COMMUNICATIONError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<minreq::Error> for CruspError {
    fn from (err: minreq::Error) -> Self {
        CruspError { 
            error_type: ErrorType::COMMUNICATIONError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<std::net::AddrParseError> for CruspError {
    fn from (err: std::net::AddrParseError) -> Self {
        CruspError {
            error_type: ErrorType::INPUTError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<std::str::ParseBoolError> for CruspError {
    fn from (err: std::str::ParseBoolError) -> Self {
        CruspError {
            error_type: ErrorType::INPUTError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<jni::errors::Error> for CruspError {
    fn from (err: jni::errors::Error) -> Self {
        CruspError {
            error_type: ErrorType::JNIError,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}

impl From<std::num::ParseIntError> for CruspError {
    fn from (err: std::num::ParseIntError) -> Self {
        CruspError {
            error_type: ErrorType::Error,
            msg: err.to_string(),
            time: SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos(),
        }
    }
}