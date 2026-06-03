import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSortParalelo {

    // Definição do Threshold (Ponto de Corte). 
    // Subvetores menores que este tamanho não gerarão novas tarefas paralelas.
    private static final int THRESHOLD = 2000;

    /**
     * Método responsável por criar e preencher um vetor com números inteiros aleatórios.
     * @param tamanho O tamanho do vetor a ser gerado.
     * @return Um vetor de inteiros preenchido com valores aleatórios.
     */
    public static int[] preencherVetorAleatorio(int tamanho) {
        int[] vetor = new int[tamanho];
        Random gerador = new Random();
        
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = gerador.nextInt(1000); 
        }
        
        return vetor;
    }

    /**
     * Método que combina (funde) dois subvetores ordenados de forma sequencial.
     * @param vetor O vetor principal.
     * @param esquerda O índice inicial do primeiro subvetor.
     * @param meio O índice final do primeiro subvetor.
     * @param direita O índice final do segundo subvetor.
     */
    public static void merge(int[] vetor, int esquerda, int meio, int direita) {
        int n1 = meio - esquerda + 1;
        int n2 = direita - meio;

        int[] vetorEsquerda = new int[n1];
        int[] vetorDireita = new int[n2];

        for (int i = 0; i < n1; ++i)
            vetorEsquerda[i] = vetor[esquerda + i];
        for (int j = 0; j < n2; ++j)
            vetorDireita[j] = vetor[meio + 1 + j];

        int i = 0, j = 0;
        int k = esquerda;
        
        while (i < n1 && j < n2) {
            if (vetorEsquerda[i] <= vetorDireita[j]) {
                vetor[k] = vetorEsquerda[i];
                i++;
            } else {
                vetor[k] = vetorDireita[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            vetor[k] = vetorEsquerda[i];
            i++;
            k++;
        }

        while (j < n2) {
            vetor[k] = vetorDireita[j];
            j++;
            k++;
        }
    }

    /**
     * Método auxiliar puramente sequencial.
     * Será invocado quando o tamanho do subvetor for menor que o THRESHOLD,
     * cortando o overhead de criação de novas tarefas da JVM.
     */
    public static void mergeSortSequencial(int[] vetor, int esquerda, int direita) {
        if (esquerda < direita) {
            int meio = esquerda + (direita - esquerda) / 2;

            mergeSortSequencial(vetor, esquerda, meio);
            mergeSortSequencial(vetor, meio + 1, direita);

            merge(vetor, esquerda, meio, direita);
        }
    }

    /**
     * Classe interna que estende RecursiveAction para gerenciar o paralelismo.
     * Funciona de forma análoga às Tasks implementadas no OpenMP.
     */
    private static class MergeSortTask extends RecursiveAction {
        private final int[] vetor;
        private final int esquerda;
        private final int direita;

        public MergeSortTask(int[] vetor, int esquerda, int direita) {
            this.vetor = vetor;
            this.esquerda = esquerda;
            this.direita = direita;
        }

        @Override
        protected void compute() {
            // Avalia o Ponto de Corte (Threshold)
            // Se o subvetor atual for pequeno demais, abandona o paralelismo e resolve sequencialmente
            if ((direita - esquerda) < THRESHOLD) {
                mergeSortSequencial(vetor, esquerda, direita);
                return;
            }

            if (esquerda < direita) {
                int meio = esquerda + (direita - esquerda) / 2;

                // Criação das tarefas independentes para cada metade do vetor
                MergeSortTask tarefaEsquerda = new MergeSortTask(vetor, esquerda, meio);
                MergeSortTask tarefaDireita = new MergeSortTask(vetor, meio + 1, direita);

                // Dispara as tarefas em paralelo e aguarda obrigatoriamente a conclusão de ambas
                invokeAll(tarefaEsquerda, tarefaDireita);

                // Após a sincronização das threads, realiza a intercalação dos dados ordenados
                merge(vetor, esquerda, meio, direita);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Iniciando testes de desempenho do MergeSort Paralelo em Java...\n");
        
        // Instancia o pool configurado explicitamente com 8 threads para pareamento com o OpenMP
        ForkJoinPool pool = new ForkJoinPool(8);
        
        try (PrintWriter logWriter = new PrintWriter(new FileWriter("log_mergesort_paralelo_java.txt", true))) {
            logWriter.println("--- Novo Teste de Desempenho (MergeSort Paralelo em Java) ---");
            
            // Laço parametrizado até o limite físico seguro de estabilidade (n = 20)
            for (int n = 0; n <= 25; n++) {
                int tamanho = (int) Math.pow(2, n) * 1000;
                
                int[] meuVetor = preencherVetorAleatorio(tamanho);
                
                // Captura o tempo inicial do clock do sistema em milissegundos
                long tempoInicial = System.currentTimeMillis();
                
                // Inicia a execução da tarefa paralela raiz dentro do pool gerenciado
                pool.invoke(new MergeSortTask(meuVetor, 0, tamanho - 1));
                
                long tempoFinal = System.currentTimeMillis();
                long tempoExecucao = tempoFinal - tempoInicial;
                
                String resultado = tamanho + " elements => " + tempoExecucao + " ms";
                System.out.println(resultado);
                logWriter.println(resultado);
            }
            
            System.out.println("\nResultados salvos com sucesso no arquivo 'log_mergesort_paralelo_java.txt'.");
            
        } catch (IOException e) {
            System.err.println("Erro ao tentar gravar o arquivo de log: " + e.getMessage());
        } finally {
            // Garante o encerramento ordenado das threads do pool ao fechar o programa
            pool.shutdown();
        }
    }
}