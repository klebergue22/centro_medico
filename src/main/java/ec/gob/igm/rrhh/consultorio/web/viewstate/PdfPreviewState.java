package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

public class PdfPreviewState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean certificadoListo;
    private boolean fichaPdfListo;
    private String pdfObjectUrl;
    private String pdfTokenFicha;
    private String pdfTokenCertificado;

    public boolean isCertificadoListo() { return certificadoListo; }
    public void setCertificadoListo(boolean certificadoListo) { this.certificadoListo = certificadoListo; }
    public boolean isFichaPdfListo() { return fichaPdfListo; }
    public void setFichaPdfListo(boolean fichaPdfListo) { this.fichaPdfListo = fichaPdfListo; }
    public String getPdfObjectUrl() { return pdfObjectUrl; }
    public void setPdfObjectUrl(String pdfObjectUrl) { this.pdfObjectUrl = pdfObjectUrl; }
    public String getPdfTokenFicha() { return pdfTokenFicha; }
    public void setPdfTokenFicha(String pdfTokenFicha) { this.pdfTokenFicha = pdfTokenFicha; }
    public String getPdfTokenCertificado() { return pdfTokenCertificado; }
    public void setPdfTokenCertificado(String pdfTokenCertificado) { this.pdfTokenCertificado = pdfTokenCertificado; }
}
