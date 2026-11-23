
@Composable
fun ScheduledMsgs(navController: NavController, viewModel: ChatViewModel, state: AppState, scheduler: AndroidAlarmSch) {
    val scheduledMsgs = state.userData?.scheduledMsgs ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.blurry_gradient_haikei),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.mensajes_programados),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (scheduledMsgs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_hay_mensajes_programados),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.mantenga_presionado_el_bot_n_de_env_o_para_programar_sus_mensajes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E).copy(alpha = 0.8f))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(scheduledMsgs) { msg ->
                            ScheduledMsgItem(scheduledMsg = msg, cancel = {
                                val instant = Instant.ofEpochSecond(it.time?.seconds ?: 0, it.time?.nanoseconds?.toLong() ?: 0)
                                val alarmItem = AlarmItem(
                                    chatId = it.chatId,
                                    content = it.content,
                                    senderId = it.senderId,
                                    time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                                )
                                alarmItem.let(scheduler::cancel)
                                viewModel.cancelScheduledMsg(it.chatId, it.content)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduledMsgItem(scheduledMsg: ScheduledMsg, cancel:(ScheduledMsg)->Unit) {
    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = scheduledMsg.ppurl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.to_user_format, scheduledMsg.username),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.scheduled_for_format, scheduledMsg.time?.let { formatter.format(it.toDate()) } ?: stringResource(R.string.na)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF238CDD)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = scheduledMsg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    maxLines = if (expanded) 10 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
                
                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cancel(scheduledMsg) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel_schedule), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
