package mobi.heron

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
data class Config(
    val UserPoolId: String,
    val Region: String,
    val ClientId: String,
    val IdentityPoolId: String,
    val ConfigBucket: String,
    val VideoBucket: String,
    val SignalQueueURL: String,
)
