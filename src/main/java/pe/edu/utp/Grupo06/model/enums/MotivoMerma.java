package pe.edu.utp.Grupo06.model.enums;

public enum MotivoMerma {
    VENCIMIENTO("Vencimiento / Caducidad"),
    ROTURA_DANO("Rotura o Daño Físico"),
    DETERIORO("Deterioro / Descomposición"),
    MERMA_OPERATIVA("Manipulación / Merma Operativa"),
    OTRO("Otro Motivo");

    private final String descripcion;

    MotivoMerma(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
