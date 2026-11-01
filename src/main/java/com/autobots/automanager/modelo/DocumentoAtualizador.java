package com.autobots.automanager.modelo;

import com.autobots.automanager.dto.Dados.DocumentoDados;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import org.springframework.stereotype.Component;

@Component
public class DocumentoAtualizador {
    public void atualizar(Documento entidade, DocumentoDados dados) {
        entidade.setTipo(dados.tipo());
        entidade.setNumero(dados.numero());
        entidade.setDataEmissao(dados.dataEmissao());
    }

    /**
     * Cria um documento e o adiciona ao usuário. A unicidade por empresa é garantida pelo banco.
     *
     * @param usuario dono do documento
     * @param dados documento informado
     * @return documento adicionado, ainda sem id até o próximo flush
     */
    public Documento adicionar(Usuario usuario, DocumentoDados dados) {
        var documento = new Documento();
        documento.setEmpresaId(usuario.getEmpresa().getId());
        atualizar(documento, dados);
        usuario.getDocumentos().add(documento);
        return documento;
    }
}
