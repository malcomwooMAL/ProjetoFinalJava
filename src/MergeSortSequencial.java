import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class MergeSortSequencial {

    /**
     * Método responsável por criar e preencher um vetor com números inteiros aleatórios.
     * * @param tamanho O tamanho do vetor a ser gerado.
     * @return Um vetor de inteiros preenchido com valores aleatórios.
     */
    public static int[] preencherVetorAleatorio(int tamanho) {
        int[] vetor = new int[tamanho];
        Random gerador = new Random();
        
        for (int i = 0; i < tamanho; i++) {
            // Gera um número inteiro aleatório entre 0 e 999           
            vetor[i] = gerador.nextInt(1000); 
        }
        
        return vetor;
    }

    /**
     * Método principal do MergeSort que divide o vetor recursivamente.
     * @param vetor O vetor a ser ordenado.
     * @param esquerda O índice inicial do subvetor.
     * @param direita O índice final do subvetor.
     */
    public static void mergeSort(int[] vetor, int esquerda, int direita) {
        if (esquerda < direita) {
            // Encontra o ponto médio para dividir o vetor em duas metades
            // A fórmula (esquerda + direita) / 2 poderia causar overflow em vetores gigantes,
            // então usamos esquerda + (direita - esquerda) / 2 por segurança.
            int meio = esquerda + (direita - esquerda) / 2;

            // Ordena a primeira e a segunda metade recursivamente
            mergeSort(vetor, esquerda, meio);
            mergeSort(vetor, meio + 1, direita);

            // Junta as metades ordenadas
            merge(vetor, esquerda, meio, direita);
        }
    }

    /**
     * Método que combina (funde) dois subvetores ordenados de forma sequencial.
     * @param vetor O vetor principal.
     * @param esquerda O índice inicial do primeiro subvetor.
     * @param meio O índice final do primeiro subvetor.
     * @param direita O índice final do segundo subvetor.
     */
    public static void merge(int[] vetor, int esquerda, int meio, int direita) {
        // Define o tamanho dos dois subvetores a serem fundidos
        int n1 = meio - esquerda + 1;
        int n2 = direita - meio;

        // Cria vetores temporários para armazenar as cópias das metades
        int[] vetorEsquerda = new int[n1];
        int[] vetorDireita = new int[n2];

        // Copia os dados originais para os vetores temporários
        for (int i = 0; i < n1; ++i)
            vetorEsquerda[i] = vetor[esquerda + i];
        for (int j = 0; j < n2; ++j)
            vetorDireita[j] = vetor[meio + 1 + j];

        // Índices iniciais dos primeiros e segundos subvetores
        int i = 0, j = 0;

        // Índice inicial de onde os elementos ordenados voltarão para o vetor principal
        int k = esquerda;
        
        // Compara elemento a elemento e os insere ordenadamente de volta no vetor original
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

        // Caso ainda restem elementos no vetorEsquerda, copia-os
        while (i < n1) {
            vetor[k] = vetorEsquerda[i];
            i++;
            k++;
        }

        // Caso ainda restem elementos no vetorDireita, copia-os
        while (j < n2) {
            vetor[k] = vetorDireita[j];
            j++;
            k++;
        }
    }

    public static void main(String[] args) {
        System.out.println("Iniciando testes de desempenho do MergeSort Sequencial...\n");
        
        // Utiliza try-with-resources para garantir que o arquivo de log seja fechado corretamente
        try (PrintWriter logWriter = new PrintWriter(new FileWriter("log_mergesort_sequencial.txt", true))) {
            logWriter.println("--- Novo Teste de Desempenho (MergeSort Sequencial) ---");
            
            // Laço para variar o expoente n de 0 até 6, conforme solicitado
            for (int n = 0; n <= 25; n++) {
                // Calcula o tamanho do vetor usando a fórmula 2^n * 1000
                int tamanho = (int) Math.pow(2, n) * 1000;
                
                // Preenche o vetor com valores aleatórios para o teste atual
                int[] meuVetor = preencherVetorAleatorio(tamanho);
                
                // Marca o tempo de início em milissegundos
                long tempoInicial = System.currentTimeMillis();
                
                // Executa a ordenação do vetor inteiro
                mergeSort(meuVetor, 0, tamanho - 1);
                
                // Marca o tempo de fim em milissegundos
                long tempoFinal = System.currentTimeMillis();
                
                // Calcula o tempo total de execução
                long tempoExecucao = tempoFinal - tempoInicial;
                
                // Formata a string com o resultado
                String resultado = tamanho + " elements => " + tempoExecucao + " ms";
                
                // Exibe o resultado no console para acompanhamento em tempo real
                System.out.println(resultado);
                
                // Grava o mesmo resultado no arquivo de log
                logWriter.println(resultado);
            }
            
            System.out.println("\nResultados salvos com sucesso no arquivo 'log_mergesort_sequencial.txt'.");
            
        } catch (IOException e) {
            System.err.println("Erro ao tentar gravar o arquivo de log: " + e.getMessage());
        }
    }
}