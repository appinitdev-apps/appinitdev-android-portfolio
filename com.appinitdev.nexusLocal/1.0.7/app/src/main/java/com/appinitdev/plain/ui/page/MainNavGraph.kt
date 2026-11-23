
@Composable
fun MainNavGraph(
    navController: NavHostController,
    mainVM: MainViewModel,
    audioPlaylistVM: AudioPlaylistViewModel,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
    notesVM: NotesViewModel,
    feedTagsVM: TagsViewModel,
    noteTagsVM: TagsViewModel,
    pomodoroVM: PomodoroViewModel,
) {
    val updateVM: UpdateViewModel = viewModel()
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        navController = navController,
        startDestination = Routing.Home,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = LinearOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(150, 50, easing = LinearOutSlowInEasing))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300, easing = FastOutLinearInEasing),
            ) + fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300, easing = LinearOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(150, 50, easing = LinearOutSlowInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = FastOutLinearInEasing),
            ) + fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing))
        },
    ) {
        composable<Routing.Home> { HomePage(navController, mainVM, updateVM, peerVM, channelVM) }
        composable<Routing.Images> { ImagesPage(navController) }
        composable<Routing.Videos> { VideosPage(navController) }
        composable<Routing.Audio> { AudioPage(navController, audioPlaylistVM) }
        composable<Routing.ChatList> { ChatListPage(navController, mainVM, peerVM = peerVM, channelVM = channelVM) }
        composable<Routing.Apps> { AppsPage(navController) }
        composable<Routing.Docs> { DocsPage(navController) }
        composable<Routing.Notes> { NotesPage(navController, notesVM = notesVM, tagsVM = noteTagsVM) }
        composable<Routing.SoundMeter> { SoundMeterPage(navController) }
        composable<Routing.PomodoroTimer> { PomodoroPage(navController, pomodoroVM = pomodoroVM) }
        composable<Routing.Settings> { SettingsPage(navController, updateVM) }
        composable<Routing.DarkTheme> { DarkThemePage(navController) }
        composable<Routing.Language> { LanguagePage(navController) }
        composable<Routing.BackupRestore> { BackupRestorePage(navController) }
        composable<Routing.WebSettings> { WebSettingsPage(navController) }
        composable<Routing.CustomFeatures> { HomeFeaturesSelectionPage(navController) }
        composable<Routing.NotificationSettings> { NotificationSettingsPage(navController) }
        composable<Routing.Connections> { ConnectionsPage(navController) }
        composable<Routing.WebDev> { WebDevPage(navController) }
        composable<Routing.WebSecurity> { WebSecurityPage(navController) }
        composable<Routing.Chat> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.Chat>()
            ChatPage(navController, audioPlaylistVM = audioPlaylistVM, chatVM = chatVM, peerVM = peerVM, channelVM = channelVM, r.id)
        }
        composable<Routing.ChatInfo> {
            ChatInfoPage(navController, chatVM = chatVM, peerVM = peerVM, channelVM = channelVM)
        }
        composable<Routing.ScanHistory> { ScanHistoryPage(navController) }
        composable<Routing.Scan> { ScanPage(navController) }
        composable<Routing.Feeds> { FeedsPage(navController) }
        composable<Routing.FeedSettings> { FeedSettingsPage(navController) }
        composable<Routing.HowToUse> { HowToUsePage(navController) }
        composable<Routing.AppDetails> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.AppDetails>()
            AppPage(navController, r.id)
        }
        composable<Routing.FeedEntries> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.FeedEntries>()
            FeedEntriesPage(navController, r.feedId, tagsVM = feedTagsVM)
        }
        composable<Routing.FeedEntry> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.FeedEntry>()
            FeedEntryPage(navController, r.id, tagsVM = feedTagsVM)
        }
        composable<Routing.NotesCreate> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.NotesCreate>()
            NotePage(navController, "", r.tagId, notesVM = notesVM, tagsVM = noteTagsVM)
        }
        composable<Routing.NoteDetail> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.NoteDetail>()
            NotePage(navController, r.id, "", notesVM = notesVM, tagsVM = noteTagsVM)
        }
        composable<Routing.Text> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.Text>()
            TextPage(navController, r.title, r.content, r.language)
        }
        composable<Routing.TextFile> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.TextFile>()
            TextFilePage(navController, r.path, r.title, r.mediaId, r.type)
        }
        composable<Routing.ChatText> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.ChatText>()
            ChatTextPage(navController, r.content)
        }
        composable<Routing.ChatEditText> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.ChatEditText>()
            ChatEditTextPage(navController, r.id, r.content, chatVM)
        }
        composable<Routing.OtherFile> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.OtherFile>()
            OtherFilePage(navController, r.path, r.title)
        }
        composable<Routing.PdfViewer> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.PdfViewer>()
            PdfPage(navController, Uri.parse(r.uri), r.fileName)
        }
        composable<Routing.Files> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.Files>()
            FilesPage(navController, audioPlaylistVM, r.folderPath)
        }
        composable<Routing.AppFiles> {
            AppFilesPage(navController)
        }
        composable<Routing.Nearby> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.Nearby>()
            NearbyPage(navController, pairDeviceJson = r.pairDeviceJson)
        }
        composable<Routing.ComponentShowcase> { ComponentShowcasePage(navController) }
        composable<Routing.DlnaReceiver> { DlnaReceiverPage(navController) }
        composable<Routing.DlnaCastHistory> { DlnaCastHistoryPage(navController) }
        composable<Routing.PlayMedia> { backStackEntry ->
            val r = backStackEntry.toRoute<Routing.PlayMedia>()
            PlayMediaPage(navController, r.path, audioPlaylistVM)
        }
    }
}
