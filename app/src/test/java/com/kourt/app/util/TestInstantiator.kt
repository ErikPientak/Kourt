package com.kourt.app.util

/**
 * Test-only helper that allocates an instance of a class without invoking any constructor.
 *
 * Used by hand-written fakes that subclass repository classes whose primary constructors
 * require Firebase dependencies (FirebaseAuth, FirebaseFirestore). Since the fakes override
 * every method that the test exercises, the underlying Firebase fields are never read,
 * so it is safe to leave them uninitialised.
 *
 * Implemented via `sun.misc.Unsafe.allocateInstance`, accessed through reflection so that
 * Kotlin source does not need a direct compile-time reference to the `sun.*` package
 * (which is closed off from the user-class compilation unit on modern JDKs). The method
 * itself is still available at runtime via `jdk.unsupported`, which is included in the
 * default JDK image.
 *
 * If the runtime JDK ever removes Unsafe, every fake construction will fail loudly the
 * first time `allocate` is invoked rather than silently misbehaving.
 */
internal object TestInstantiator {

    private val unsafe: Any by lazy {
        val cls = Class.forName("sun.misc.Unsafe")
        val field = cls.getDeclaredField("theUnsafe")
        field.isAccessible = true
        field.get(null) ?: error("sun.misc.Unsafe.theUnsafe was null")
    }

    private val allocateInstance: java.lang.reflect.Method by lazy {
        Class.forName("sun.misc.Unsafe")
            .getMethod("allocateInstance", Class::class.java)
    }

    /**
     * Allocate an instance of [cls] without calling any constructor or initialising fields.
     */
    fun <T : Any> allocate(cls: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return allocateInstance.invoke(unsafe, cls) as T
    }

    inline fun <reified T : Any> allocate(): T = allocate(T::class.java)
}
