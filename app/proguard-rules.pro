# Keep line numbers and source file names for readable crash reports in Play Console
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all annotations at runtime — required by Firestore (@PropertyName, @DocumentId),
# Hilt, and Room
-keepattributes *Annotation*

# ── Firestore data models ──────────────────────────────────────────────────────────────
# Firestore deserializes documents into these classes via reflection.
# Field names that lack @PropertyName must survive obfuscation intact.
-keep class com.kourt.app.data.model.** { *; }

# ── Room entities ──────────────────────────────────────────────────────────────────────
# Room generates code at compile time, but the entity field names must match the DB schema.
-keep class com.kourt.app.data.local.**Entity { *; }

# ── WorkManager workers ────────────────────────────────────────────────────────────────
# WorkManager stores the worker class name as a string in the job database.
# If the class is renamed, enqueued jobs will fail to restart after process death.
-keep class com.kourt.app.worker.** { *; }
