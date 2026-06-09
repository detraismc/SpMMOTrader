package me.detraismc.spmmotrader.gui;

public class GUISession {

    private final String module;
    private final int page;
    private final String guiName;

    public GUISession(String module, int page, String guiName) {
        this.module = module;
        this.page = page;
        this.guiName = guiName;
    }

    public String getModule() { return module; }
    public int getPage() { return page; }
    public String getGuiName() { return guiName; }
}
