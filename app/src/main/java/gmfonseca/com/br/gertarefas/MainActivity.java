package gmfonseca.com.br.gertarefas;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import gmfonseca.com.br.gertarefas.api.Tarefa;
import gmfonseca.com.br.gertarefas.api.TarefaService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity {

    static final String BACKEND_URL = "http://192.168.0.9:8080";

    public static List<Tarefa> tarefas = new ArrayList<>();

    private RecyclerView tarefasList;
    private TextView semTarefa;
    private AlertDialog.Builder concluindoDialog;

    private boolean continuar=false;

    private TarefaService tarefaService;
    private Handler handleUpdater = new Handler();
    private Runnable repeater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tarefasList = findViewById(R.id.tarefasList);
        semTarefa = findViewById(R.id.semTarefa);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        tarefaService = retrofit.create( TarefaService.class );

        //ADAPTER RECEBE OS DADOS E FORMATA O LAYOUT
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
        tarefasList.setLayoutManager(manager);
        tarefasList.setHasFixedSize(true);
        tarefasList.addItemDecoration( new DividerItemDecoration(this, LinearLayout.VERTICAL));

        initTarefasListUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdater();
    }

    /**
     * Iniciar o loop de requisições ao backend
     */
    private void startUpdater(){
        repeater.run();
    }

    /**
     * Parar o loop de requisições ao backend
     */
    private void stopUpdater(){
        handleUpdater.removeCallbacks(repeater);
    }

    /**
     * Iniciar as variaveis do loop de requisições pro backend com um
     * intervalo de 10 segundos.
     */
    private void initTarefasListUpdate(){
        this.repeater = new Runnable() {
            @Override
            public void run() {
                getAllTarefas();
                handleUpdater.postDelayed(this, 10000);
            }
        };
    }

    /**
     * Conexão com o backend para requisitar as tarefas pendentes
     * com a biblioteca retrofit.
     */
    private void getAllTarefas(){

        Call<List<Tarefa>> call = tarefaService.recuperarTarefas();

        call.enqueue(new Callback<List<Tarefa>>() {

            @Override
            public void onResponse(Call<List<Tarefa>> call, Response<List<Tarefa>> response) {
                if(response.isSuccessful()){
                    tarefas = response.body();
                    tarefasList.setAdapter(new Adapter(tarefas, MainActivity.this));
                }else{
                    Toast.makeText(MainActivity.this, "Não foi possível comnicar com o servidor.", Toast.LENGTH_LONG).show();
                }

                if(tarefas.size()==0) {
                    semTarefa.setVisibility(View.VISIBLE);
                }else {
                    semTarefa.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Tarefa>> call, Throwable t) {
                if(tarefas.size() == 0)
                    semTarefa.setVisibility(View.VISIBLE);
                else
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();
                bitmap.recycle();

                int tarefaId = Adapter.CLICKED_TAREFA.getId();

                concluirTarefa(tarefaId, imageBytes);

                continuar=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Conexão com o backend para concluir uma tarefa especifica
     * com a biblioteca retrofit.
     */
    private void concluirTarefa(int tarefaId, byte[] imagem){

        RequestBody reqBody = RequestBody.create(MediaType.parse("image/jpeg"), imagem);
        MultipartBody.Part imageBody = MultipartBody.Part.createFormData("image", "resolucao", reqBody);

        Call<Tarefa> call = tarefaService.concluirTarefa(tarefaId, imageBody);

        call.enqueue(new Callback<Tarefa>() {

            @Override
            public void onResponse(Call<Tarefa> call, Response<Tarefa> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Tarefa concluída com sucesso.", Toast.LENGTH_LONG).show();
                    getAllTarefas();
                } else {
                    Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Tarefa> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
