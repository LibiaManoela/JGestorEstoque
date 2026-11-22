package br.com.estoque.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

//Regras de validação dos campos do formulário (obrigatórios, formato de SKU e intervalos numéricos).
public class ValidadorEstoque {
    private static final Pattern PADRAO_SKU = Pattern.compile("^[A-Z0-9-]{4,15}$");

    public Map<String, String> validarCamposBrutos(
            String nome,
            String sku,
            String categoria,
            String quantidadeTexto,
            String estoqueMinimoTexto,
            String precoTexto
    ) {
        Map<String, String> erros = new LinkedHashMap<>();

        if (nome == null || nome.trim().isEmpty()) {
            erros.put("nome", "O nome do item é obrigatório.");
        } else if (nome.trim().length() < 3 || nome.trim().length() > 80) {
            erros.put("nome", "O nome deve ter entre 3 e 80 caracteres.");
        }

        if (sku == null || sku.trim().isEmpty()) {
            erros.put("sku", "O código SKU é obrigatório.");
        } else if (!PADRAO_SKU.matcher(sku.trim().toUpperCase()).matches()) {
            erros.put("sku", "Use de 4 a 15 caracteres (letras, números ou hífen, ex: MAT-001).");
        }

        if (categoria == null || categoria.trim().isEmpty()) {
            erros.put("categoria", "Selecione uma categoria.");
        }

        if (quantidadeTexto == null || quantidadeTexto.trim().isEmpty()) {
            erros.put("quantidade", "A quantidade atual é obrigatória.");
        } else {
            try {
                int quantidade = Integer.parseInt(quantidadeTexto.trim());
                if (quantidade < 0 || quantidade > 100000) {
                    erros.put("quantidade", "A quantidade deve estar entre 0 e 100.000.");
                }
            } catch (NumberFormatException e) {
                erros.put("quantidade", "Informe um número inteiro válido.");
            }
        }

        if (estoqueMinimoTexto == null || estoqueMinimoTexto.trim().isEmpty()) {
            erros.put("estoqueMinimo", "O estoque mínimo é obrigatório.");
        } else {
            try {
                int minimo = Integer.parseInt(estoqueMinimoTexto.trim());
                if (minimo < 1 || minimo > 1000) {
                    erros.put("estoqueMinimo", "O estoque mínimo deve estar entre 1 e 1.000.");
                }
            } catch (NumberFormatException e) {
                erros.put("estoqueMinimo", "Informe um número inteiro válido.");
            }
        }

        if (precoTexto == null || precoTexto.trim().isEmpty()) {
            erros.put("preco", "O preço unitário é obrigatório.");
        } else {
            try {
                double preco = Double.parseDouble(precoTexto.trim().replace(",", "."));
                if (preco <= 0.0 || preco > 1000000.0) {
                    erros.put("preco", "O preço deve ser maior que R$ 0,00 e até R$ 1.000.000,00.");
                }
            } catch (NumberFormatException e) {
                erros.put("preco", "Informe um valor numérico válido (ex: 25,90).");
            }
        }

        return erros;
    }
}
