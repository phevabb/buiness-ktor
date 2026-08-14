package account.utils



fun normalizeTenantCodeForTenantService(
    tenantCode: String
): String {

    return tenantCode
        .trim()
        .lowercase()
        .replace("-", "")
        .replace("_", "")
        .replace(" ", "")
}