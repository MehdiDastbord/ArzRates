package com.arz.rates.data

import kotlinx.serialization.Serializable

@Serializable
data class Rate(
    val value: String? = null,
    val change: Double? = null,
    val timestamp: Long? = null,
    val date: String? = null,
    val change_val: Double? = null,
    val change_pct: Double? = null
) {
    fun numericValue(): Double? = value?.replace(",", "")?.toDoubleOrNull()
}

data class RateItem(
    val key: String,
    val rate: Rate
) {
    val title: String get() = CurrencyNames.nameFor(key)
    val symbol: String get() = CurrencyNames.symbolFor(key)
}

object CurrencyNames {
    private val names = mapOf(
        "usd" to "US Dollar", "eur" to "Euro", "gbp" to "British Pound",
        "aed" to "UAE Dirham", "sar" to "Saudi Riyal", "qar" to "Qatari Riyal",
        "kwd" to "Kuwaiti Dinar", "omr" to "Omani Rial", "bhd" to "Bahraini Dinar",
        "try" to "Turkish Lira", "jpy" to "Japanese Yen", "cny" to "Chinese Yuan",
        "inr" to "Indian Rupee", "cad" to "Canadian Dollar", "aud" to "Australian Dollar",
        "chf" to "Swiss Franc", "nzd" to "New Zealand Dollar", "rub" to "Russian Ruble",
        "krw" to "South Korean Won", "thb" to "Thai Baht", "myr" to "Malaysian Ringgit",
        "sek" to "Swedish Krona", "nok" to "Norwegian Krone", "dkk" to "Danish Krone",
        "pln" to "Polish Zloty", "zar" to "South African Rand", "mxn" to "Mexican Peso",
        "brl" to "Brazilian Real", "sgd" to "Singapore Dollar", "hkd" to "Hong Kong Dollar",
        "idr" to "Indonesian Rupiah", "pkr" to "Pakistani Rupee", "iqd" to "Iraqi Dinar",
        "afn" to "Afghan Afghani", "xau" to "Gold", "xag" to "Silver",
        "usd_xau" to "Gold / USD", "bub_sekkeh" to "Emami Coin",
        "bub_bahar" to "Bahar Azadi Coin", "bub_nim" to "Half Coin",
        "bub_rob" to "Quarter Coin", "bub_18ayar" to "18K Gold",
        "bub_gerami" to "Gerami Coin", "mob_usd" to "Mobile USD",
        "mob_gbp" to "Mobile GBP", "mob_eur" to "Mobile EUR", "mob_aed" to "Mobile AED"
    )

    fun nameFor(key: String): String {
        return names[key] ?: key.replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
    }

    fun symbolFor(key: String): String = when (key) {
        "usd" -> "$"; "eur" -> "€"; "gbp" -> "£"; "aed" -> "د.إ"
        "sar" -> "﷼"; "jpy" -> "¥"; "cny" -> "¥"; "inr" -> "₹"
        else -> key.uppercase()
    }
}
