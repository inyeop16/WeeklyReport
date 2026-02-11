package com.pluxity.weeklyreport.exception

class ResourceNotFoundException(
    val resourceName: String,
    val fieldName: String,
    val fieldValue: Any
) : RuntimeException("$resourceName not found with $fieldName: $fieldValue")
