
 

















class TrialExpired : PanelFragment() {

    private lateinit var binding: FragmentTrialExpiredBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTrialExpiredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()

        val gracePeriodExpired = TrialPreferences.isGracePeriodExpired()

        if (gracePeriodExpired) {
            binding.trialMessage.text = getString(R.string.grace_period_expired_message)
            binding.graceCounter.gone(animate = false)
            binding.closeOrLater.text = getString(R.string.close)
            binding.closeOrLater.setOnClickListener {
                requireActivity().finish()
            }
        } else {
            binding.trialMessage.text = getString(R.string.trial_expired_message)
            val used = TrialPreferences.getGraceLaunchesUsed()
            val max = TrialPreferences.MAX_GRACE_LAUNCHES
            binding.graceCounter.text = getString(R.string.grace_launches_used, used, max)
            binding.closeOrLater.text = getString(R.string.purchase_later)
            binding.closeOrLater.setOnClickListener {
                TrialPreferences.incrementGraceLaunches()
                (requireActivity() as MainActivity).showHome()
            }
        }

        binding.purchaseNow.setOnClickListener {
            openFragment(Purchase.newInstance(), Purchase.TAG)
        }
    }

    companion object {
         




        fun newInstance(): TrialExpired {
            val args = Bundle()
            val fragment = TrialExpired()
            fragment.arguments = args
            return fragment
        }

         
        const val TAG = "TrialExpired"
    }
}

