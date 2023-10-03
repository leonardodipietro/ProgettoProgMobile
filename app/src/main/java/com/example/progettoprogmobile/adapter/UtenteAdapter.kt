import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Utente

class UtenteAdapter : RecyclerView.Adapter<UtenteAdapter.UtenteViewHolder>() {

    private var users = listOf<Utente>()

    fun submitList(users: List<Utente>) {
        this.users = users
        Log.d("UtenteAdapter", "submitList - users.size: ${users.size}")
        notifyDataSetChanged() // Notifica all'adapter che i dati sono cambiati
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtenteViewHolder {
        Log.d("UtenteAdapter", "onCreateViewHolder called")
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_utente_view, parent, false)
        return UtenteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UtenteViewHolder, position: Int) {
        Log.d("UtenteAdapter", "onBindViewHolder - position: $position")
        val user= users[position]
        Log.d("UtenteAdapter", "User at position $position: ${user.toString()}")
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        Log.d("UtenteAdapter", "getItemCount: ${users.size}")
        return users.size
    }


    class UtenteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(utente: Utente) {
            val textViewNome: TextView = itemView.findViewById(R.id.textViewNome)
            if(utente.name.isNullOrEmpty()) {
                Log.e("UtenteViewHolder", "User name is null or empty")
            } else {
                Log.d("UtenteViewHolder", "Binding user name: ${utente.name}")
                textViewNome.text = utente.name
            }
        }
    }

}
