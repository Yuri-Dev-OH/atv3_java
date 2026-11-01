package com.autobots.automanager.entidades;

import com.autobots.automanager.enumeracoes.TipoCredencial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Acesso por código de barras, como um crachá de funcionário. */
@Getter
@Setter
@Entity
public class CredencialCodigoBarra extends Credencial {
    @Column(nullable = false, unique = true)
    private Long codigo;

    @Override
    public TipoCredencial getTipo() { return TipoCredencial.CODIGO_BARRA; }
}
