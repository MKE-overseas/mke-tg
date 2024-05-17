# MKE tg utils

## Admin menu

1. Create feature instance in implementation of BaseLocationConfig11
```kotlin
val adminsFeature by lazy {
    AdminsFeature(
        tgUser = tgUser,
        userSelector = { userId: Long -> transaction { ComplaintsTgUser.findById(userId) } },
        usersSelector = { transaction { ComplaintsTgUser.all() } }
    )
}
```

2. Use feature in global CallbackQueryHandler and CommandHandler
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