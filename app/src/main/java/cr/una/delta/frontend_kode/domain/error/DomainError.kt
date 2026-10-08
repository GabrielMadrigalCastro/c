package cr.una.delta.frontend_kode.domain.error

sealed class DomainError (
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    class TaskError(
        message: String,
        cause: Throwable? = null
    ) : DomainError(message, cause)

    class NetworkError(
        message: String,
        cause: Throwable? = null
    ) : DomainError(message, cause)

    class MappingError(
        message: String,
        cause: Throwable? = null
    ) : DomainError(message, cause)

    class UnknownError(
        message: String = "An unknown error occurred",
        cause: Throwable? = null
    ) : DomainError(message, cause)
}