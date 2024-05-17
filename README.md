# MKE tg utils

## Admin menu

1. Create feature instance in implementation of BaseLocationConfig
```kotlin
val adminsFeature by lazy {
    AdminsFeature(
        tgUser = tgUser,
        userSelector = { userId: Long -> transaction { ComplaintsTgUser.findById(userId) } },
        usersSelector = { transaction { ComplaintsTgUser.all() } }
    )
}
```

2. Use feature in global **CallbackQueryHandler** and **CommandHandler**
```kotlin
global {
    handleCallbackQuery(handlerId = "global") {
        if (tgUser.isAdmin) {
            setupFeatures(adminsFeature)
        }
    }

    handleCommand(handlerId = "global") {
        if (tgUser.isAdmin) {
            setupFeatures(adminsFeature)
        }
    }
}
```

## Auth via Google Sheets
1. Create private static base instance in companion object of BaseLocationConfig
```kotlin
    companion object {
        private val baseAuthGoogleSheetsFeature = AuthGoogleSheetsFeature(
            tgUser = TgUser(EntityID(0, TgUsers)),
            sheetService = SheetService,
            phonesSpreadsheetId = phonesSpreadsheetId,
            getPhone = { message.contact?.phoneNumber?.phoneFormatted() },
            onFail = { send("Вашего телефона нет среди разрешенных. ${contactEmail?.let { "Обратитесь на почту $it, чтобы вас добавили в систему." }}") },
            onSuccess = { phone, _ ->
                suspendTransaction {
                    tgUser.phone = phone
                    tgUser.isRegistered = true
                    tgUser.location = Location.MENU
                    tgUser.provideCommands()
                    sendWelcomeMessage()
                }
            }
        )
    }
```

2. Create feature instance in implementation of BaseLocationConfig
```kotlin
val authGoogleSheetsFeature by lazy {
    baseAuthGoogleSheetsFeature.copy(tgUser = tgUser)
}
```

3. Use feature in global **MessageHandler**
```kotlin
global {
    handleMessage(handlerId = "global") {
        if (!tgUser.isRegistered) {
            setupFeatures(authGoogleSheetsFeature)
        }
    }
}
```