
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.ablanco.zoomy.Zoomy
import com.boolder.boolder.databinding.FragmentTopoFullScreenBinding

class TopoFullScreenFragment : Fragment() {

    companion object {
        private const val IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String): TopoFullScreenFragment {
            val args = Bundle().apply {
                putString(IMAGE_URL, imageUrl)
            }
            return TopoFullScreenFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTopoFullScreenBinding.inflate(inflater, container, false)
        val imageUrl = arguments?.getString(IMAGE_URL)

        imageUrl?.let {
            binding.imageView.load(it) {
                allowHardware(false)
            }
        }

        binding.imageView.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        Zoomy.Builder(activity).target(binding.imageView).tapListener { activity?.supportFragmentManager?.popBackStack() }
            .register()


        return binding.root
    }
}
