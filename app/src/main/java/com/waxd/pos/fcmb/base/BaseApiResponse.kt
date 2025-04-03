package com.waxd.pos.fcmb.base

class BaseApiResponse<T>(val status: String, val data: T, val message: String)