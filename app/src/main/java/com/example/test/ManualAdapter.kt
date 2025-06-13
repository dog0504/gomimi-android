import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.test.ManualDetailActivity
import com.example.test.ManualItem
import com.example.test.R

class ManualAdapter(private val context: Context, private val manualList: List<ManualItem>) :
    RecyclerView.Adapter<ManualAdapter.ManualViewHolder>() {

    class ManualViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemButton: Button = view.findViewById(R.id.itemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManualViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.manual_list_row, parent, false)
        return ManualViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManualViewHolder, position: Int) {
        val item = manualList[position]
        holder.itemButton.text = item.title
        holder.itemButton.setOnClickListener {
            val intent = Intent(context, ManualDetailActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("description", item.description)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = manualList.size
}
