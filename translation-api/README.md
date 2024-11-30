### flashcard-kt ::: translation-api

contract:

```kotlin
interface TranslationRepository {
    suspend fun fetch(sourceLang: String, targetLang: String, word: String): TCard
}
```