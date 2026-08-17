package sms.tables





import org.jetbrains.exposed.v1.core.dao.id.IntIdTable


object SmsWalletTopUpPaymentsTable : IntIdTable("sms_wallet_topup_payments") {

    val tenantCode =
        varchar(
            name = "tenant_code",
            length = 100
        )

    val schoolName =
        varchar(
            name = "school_name",
            length = 255
        ).nullable()

    val email =
        varchar(
            name = "email",
            length = 255
        )

    val amount =
        decimal(
            name = "amount",
            precision = 12,
            scale = 2
        )

    val amountInPesewas =
        integer(
            name = "amount_in_pesewas"
        )

    val reference =
        varchar(
            name = "reference",
            length = 300
        ).uniqueIndex()

    val authorizationUrl =
        varchar(
            name = "authorization_url",
            length = 1000
        ).nullable()

    val accessCode =
        varchar(
            name = "access_code",
            length = 300
        ).nullable()

    val status =
        varchar(
            name = "status",
            length = 30
        ).default("pending")

    val verified =
        bool(
            name = "verified"
        ).default(false)

    val walletCredited =
        bool(
            name = "wallet_credited"
        ).default(false)

    val paystackStatus =
        varchar(
            name = "paystack_status",
            length = 50
        ).nullable()

    val paystackMessage =
        varchar(
            name = "paystack_message",
            length = 500
        ).nullable()

    val createdAt =
        varchar(
            name = "created_at",
            length = 50
        )

    val verifiedAt =
        varchar(
            name = "verified_at",
            length = 50
        ).nullable()

    val updatedAt =
        varchar(
            name = "updated_at",
            length = 50
        ).nullable()
}