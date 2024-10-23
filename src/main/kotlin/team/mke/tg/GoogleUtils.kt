package team.mke.tg

import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.ExtendedValue
import com.google.api.services.sheets.v4.model.TextFormat
import ru.raysmith.google.model.api.HorizontalAlign
import ru.raysmith.google.model.api.WrapStrategy
import ru.raysmith.google.sheets.utils.extendedValue
import ru.raysmith.google.sheets.utils.hyperlink
import ru.raysmith.google.utils.GoogleDSL

@GoogleDSL
fun CellData.tgHyperLinkOrText(
    user: TgUserWithBaseData<*>,
    horizontalAlignment: HorizontalAlign = HorizontalAlign.LEFT,
    wrapStrategy: WrapStrategy = WrapStrategy.WRAP
) {
    if (user.username != null) apply {
        extendedValue {
            formulaValue = tgHyperlink(user)
        }
    } else apply {
        userEnteredValue = ExtendedValue().setStringValue(user.fullname())
        userEnteredFormat = CellFormat().setTextFormat(TextFormat()).apply {
            this.horizontalAlignment = horizontalAlignment.name
            this.wrapStrategy = wrapStrategy.name
        }
    }
}

fun tgHyperlink(tgUser: TgUserWithBaseData<*>) =
    if (tgUser.username == null) tgUser.fullname()
    else hyperlink("https://t.me/${tgUser.username}", tgUser.fullname())