package app.forigon.settings

import io.github.mlmgames.settings.core.annotations.SchemaVersion

inline fun <reified T : Any> schemaVersionOf(): Int =
    T::class.java.getAnnotation(SchemaVersion::class.java)?.version ?: 0