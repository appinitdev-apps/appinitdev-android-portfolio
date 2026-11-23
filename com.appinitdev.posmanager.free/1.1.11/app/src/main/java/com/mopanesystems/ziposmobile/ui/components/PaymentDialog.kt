
data class PaymentResultInfo(
    val isCreditSale: Boolean,
    val nota: String? = null,
    val paymentMethod: PaymentMethod,
    val remaining: BigDecimal = BigDecimal.ZERO
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    customer: Customer? = null,
    total: BigDecimal,
    onDismiss: () -> Unit,
    paymentMethod: PaymentMethod,
    onProcessPayment: (PaymentResultInfo) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var creditSale by remember { mutableStateOf(false) }

    val paid = input.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val remaining = total.subtract(paid)
    val isButtonEnabled = (paid >= total) || (paid < total && creditSale && paid >= BigDecimal.ZERO)
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.total_payment) + " $${total.toPlainString()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            error = null
                            if (total.subtract(it.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                                creditSale = false
                            }
                        },
                        label = { Text(stringResource(R.string.amount_to_pay)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = {
                            input = total.toPlainString()
                            error = null
                        },
                        modifier = Modifier.height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            stringResource(R.string.full_amount),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                
                when {
                    paid > total -> {
                        val change = paid.subtract(total)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.change),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$${change.toPlainString()}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = { input = total.toPlainString() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        stringResource(R.string.pay_exact_amount),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    paid < total && paid >= BigDecimal.ZERO && input.isNotEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.amount_remaining),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "$${remaining.toPlainString()}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                if (customer != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = creditSale,
                                            onCheckedChange = { creditSale = it }
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            stringResource(R.string.request_credit_sale),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    paid == total && input.isNotEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.exact_amount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (paid <= BigDecimal.ZERO && !creditSale) {
                        error = context.getString(R.string.amount_must_be_greater_than_0)
                    } else {
                        onProcessPayment(
                            if (creditSale && paid < total) {
                                PaymentResultInfo(
                                    isCreditSale = creditSale,
                                    nota = context.getString(R.string.paid) + " $${paid.toPlainString()}  " + context.getString(R.string.remaining) + " $${remaining.toPlainString()}",
                                    paymentMethod = PaymentMethod.CREDIT_SALE,
                                    remaining = remaining
                                )
                            } else {
                                PaymentResultInfo(
                                    isCreditSale = creditSale,
                                    paymentMethod = paymentMethod
                                )
                            }
                        )
                        onDismiss()
                    }
                },
                enabled = isButtonEnabled
            ) {
                Text(
                    stringResource(R.string.process_payment),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancelar),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    )
}
