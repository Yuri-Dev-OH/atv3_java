package com.autobots.automanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.autobots.automanager.enumeracoes.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * Entradas validadas da API.
 *
 * <p>Textos usam {@link Length} em vez de {@code @Size}: a validação é a mesma, mas o HAL-FORMS publica
 * {@code maxLength} em vez de tratar o campo como intervalo numérico ({@code type: range}). Regras entre campos usam
 * {@link AssertTrue} com {@link JsonIgnore}, para não aparecerem como campos nos templates HAL-FORMS.
 */
public final class Dados {
    private Dados() {}
    private static String limpar(String valor) { return valor == null ? null : valor.strip(); }
    private static <T> List<T> lista(List<T> valores) { return valores == null ? List.of() : valores; }

    public record EmpresaCadastro(
        @NotBlank @Length(max = 150) String razaoSocial,
        @Length(max = 150) String nomeFantasia,
        @Valid EnderecoDados endereco,
        List<@NotNull @Valid TelefoneDados> telefones
    ) {
        public EmpresaCadastro {
            razaoSocial = limpar(razaoSocial); nomeFantasia = limpar(nomeFantasia); telefones = lista(telefones);
        }
    }
    public record EmpresaDados(
        @NotBlank @Length(max = 150) String razaoSocial,
        @Length(max = 150) String nomeFantasia
    ) {
        public EmpresaDados { razaoSocial = limpar(razaoSocial); nomeFantasia = limpar(nomeFantasia); }
    }
    public record UsuarioCadastro(
        @NotBlank @Length(max = 150) String nome,
        @Length(max = 150) String nomeSocial,
        @PastOrPresent LocalDate dataNascimento,
        @NotEmpty Set<@NotNull PerfilUsuario> perfis,
        @Valid EnderecoDados endereco,
        List<@NotNull @Valid TelefoneDados> telefones,
        List<@NotNull @Valid DocumentoDados> documentos,
        List<@NotNull @Valid EmailDados> emails
    ) {
        public UsuarioCadastro {
            nome = limpar(nome); nomeSocial = limpar(nomeSocial);
            telefones = lista(telefones); documentos = lista(documentos); emails = lista(emails);
        }
    }
    public record UsuarioDados(
        @NotBlank @Length(max = 150) String nome,
        @Length(max = 150) String nomeSocial,
        @PastOrPresent LocalDate dataNascimento,
        @NotEmpty Set<@NotNull PerfilUsuario> perfis
    ) {
        public UsuarioDados { nome = limpar(nome); nomeSocial = limpar(nomeSocial); }
    }
    public record EnderecoDados(
        @Length(max = 100) String estado,
        @NotBlank @Length(max = 100) String cidade,
        @Length(max = 100) String bairro,
        @NotBlank @Length(max = 150) String rua,
        @NotBlank @Length(max = 20) String numero,
        @Pattern(regexp = "[0-9]{8}", message = "deve conter 8 dígitos") String codigoPostal,
        @Length(max = 255) String informacoesAdicionais
    ) {
        public EnderecoDados {
            estado = limpar(estado); cidade = limpar(cidade); bairro = limpar(bairro);
            rua = limpar(rua); numero = limpar(numero); codigoPostal = limpar(codigoPostal);
            informacoesAdicionais = limpar(informacoesAdicionais);
        }
    }
    public record DocumentoDados(
        @NotNull TipoDocumento tipo,
        @NotBlank @Length(max = 50) String numero,
        @NotNull @PastOrPresent LocalDate dataEmissao
    ) {
        public DocumentoDados { numero = limpar(numero); }
    }
    public record TelefoneDados(
        @NotNull @Pattern(regexp = "[1-9][0-9]", message = "deve conter 2 dígitos, sem zero inicial") String ddd,
        @NotNull @Pattern(regexp = "[0-9]{8,9}", message = "deve conter 8 ou 9 dígitos") String numero
    ) {
        public TelefoneDados { ddd = limpar(ddd); numero = limpar(numero); }
    }
    public record EmailDados(@NotBlank @Email @Length(max = 254) String endereco) {
        public EmailDados { endereco = endereco == null ? null : endereco.strip().toLowerCase(Locale.ROOT); }
    }

    /**
     * Credencial de acesso. {@code USUARIO_SENHA} usa {@code nomeUsuario} e {@code senha}; {@code CODIGO_BARRA} usa
     * {@code codigo}. Na atualização, {@code senha} omitida mantém a senha atual.
     */
    public record CredencialDados(
        @NotNull TipoCredencial tipo,
        @Length(min = 3, max = 50) @Pattern(regexp = "[A-Za-z0-9._-]+", message = "aceita letras, números, ponto, hífen e sublinhado") String nomeUsuario,
        @Length(min = 8, max = 128) String senha,
        @Positive Long codigo,
        boolean inativo
    ) {
        public CredencialDados { nomeUsuario = limpar(nomeUsuario); }
        @JsonIgnore
        @AssertTrue(message = "USUARIO_SENHA exige nomeUsuario e não aceita codigo; CODIGO_BARRA exige codigo e não aceita nomeUsuario nem senha")
        public boolean isCamposDoTipo() {
            if (tipo == null) return true;
            return tipo == TipoCredencial.USUARIO_SENHA ? nomeUsuario != null && codigo == null
                : codigo != null && nomeUsuario == null && senha == null;
        }
    }
    public record VeiculoDados(
        @NotNull TipoVeiculo tipo,
        @NotBlank @Length(max = 100) String modelo,
        @NotNull @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "deve seguir o formato ABC1234 ou ABC1D23") String placa
    ) {
        public VeiculoDados {
            modelo = limpar(modelo);
            placa = placa == null ? null : placa.strip().toUpperCase(Locale.ROOT).replace("-", "");
        }
    }
    public record MercadoriaDados(
        @NotBlank @Length(max = 150) String nome,
        @Length(max = 255) String descricao,
        @PastOrPresent LocalDate fabricacao,
        LocalDate validade,
        @NotNull @PositiveOrZero Long quantidade,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @Positive Long fornecedorId
    ) {
        public MercadoriaDados { nome = limpar(nome); descricao = limpar(descricao); }
        @JsonIgnore
        @AssertTrue(message = "validade não pode ser anterior à fabricação")
        public boolean isValidadeAposFabricacao() {
            return fabricacao == null || validade == null || !validade.isBefore(fabricacao);
        }
    }
    public record ServicoDados(
        @NotBlank @Length(max = 150) String nome,
        @Length(max = 255) String descricao,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor
    ) {
        public ServicoDados { nome = limpar(nome); descricao = limpar(descricao); }
    }

    /** Venda de mercadorias e serviços; cada item informa o {@code id} do catálogo e a quantidade. */
    public record VendaDados(
        @NotBlank @Length(max = 50) String identificacao,
        @NotNull @Positive Long clienteId,
        @NotNull @Positive Long funcionarioId,
        @Positive Long veiculoId,
        List<@NotNull @Valid ItemDados> mercadorias,
        List<@NotNull @Valid ItemDados> servicos
    ) {
        public VendaDados { identificacao = limpar(identificacao); mercadorias = lista(mercadorias); servicos = lista(servicos); }
        @JsonIgnore
        @AssertTrue(message = "a venda precisa de ao menos uma mercadoria ou serviço")
        public boolean isPossuiItens() { return !mercadorias.isEmpty() || !servicos.isEmpty(); }
    }
    public record ItemDados(
        @NotNull @Positive Long id,
        @NotNull @Positive @Max(10_000) Integer quantidade
    ) {}
}
