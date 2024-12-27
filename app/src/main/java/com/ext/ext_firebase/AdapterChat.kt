import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ext.ext_firebase.Chats
import com.ext.ext_firebase.databinding.RawReceiverChatBinding
import com.ext.ext_firebase.databinding.RawSenderChatBinding

class AdapterChat(private val messageList: ArrayList<Chats>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENDER = 1
        const val VIEW_TYPE_RECEIVER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val binding = RawSenderChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SenderViewHolder(binding)
        } else {
            val binding = RawReceiverChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceiverViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        if (holder is SenderViewHolder) {
            holder.binding.tvMessage.text = message.messageText
        } else if (holder is ReceiverViewHolder) {
            holder.binding.tvMessage.text = message.messageText
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SenderViewHolder(val binding: RawSenderChatBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceiverViewHolder(val binding: RawReceiverChatBinding) : RecyclerView.ViewHolder(binding.root)
}
