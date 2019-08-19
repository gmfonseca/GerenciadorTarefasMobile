package gmfonseca.com.br.gertarefas;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import gmfonseca.com.br.gertarefas.api.Tarefa;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private List<Tarefa> tarefas;
    private Activity activity;
    protected static Tarefa CLICKED_TAREFA;

    protected Adapter(List<Tarefa> tarefas, Activity activity) {
        this.tarefas = tarefas;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View tarefaView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.template_tarefa, viewGroup, false);

        return new MyViewHolder(tarefaView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        final Tarefa tarefa = tarefas.get(i);
        holder.titulo.setText(tarefa.getTitulo());
        final int id = tarefa.getId();
        holder.btn_concluir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                CLICKED_TAREFA = tarefa;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tarefas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        Button btn_concluir;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tarefaTitulo);
            btn_concluir = itemView.findViewById(R.id.btnFazer);
        }
    }

}
