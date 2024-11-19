package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.model.Serie;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.OpenAiHttpException;

public class ConsultaChatGPT {

    public static String obterTraducao(String texto) {
        OpenAiService service = new OpenAiService(System.getenv("OPENAI_APIKEY"));

        // Configurando a requisição com o padrão Builder
        CompletionRequest requisicao = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt("traduza para o português o texto: " + texto)
                .maxTokens(1000)
                .temperature(0.7)
                .build();

        try {
            var resposta = service.createCompletion(requisicao);
            return resposta.getChoices().get(0).getText();
        } catch (OpenAiHttpException e) {
            System.out.println("Erro de cota: " + e.getMessage());
            return texto;
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
            return "Erro ao obter tradução.";
        }
    }
}
