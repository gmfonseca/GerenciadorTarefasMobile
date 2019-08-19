package gmfonseca.com.br.gertarefas.api;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface TarefaService {

    @GET("/tarefas?concluido=false")
    Call<List<Tarefa>> recuperarTarefas();

    @Multipart
    @PUT("/tarefas/{id}")
    Call<Tarefa> concluirTarefa(@Path("id") int id, @Part MultipartBody.Part image);

}
