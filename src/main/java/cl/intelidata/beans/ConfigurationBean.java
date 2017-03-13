/*
 * Copyright (c) 2016, Intelidata S.A.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cl.intelidata.beans;

import cl.intelidata.services.ConfigurationService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.chart.PieChartModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DFeliu
 */
@ManagedBean
@SessionScoped
public class ConfigurationBean implements Serializable {

    private static final long serialVersionUID = -2152389656664659476L;
    private static Logger logger = LoggerFactory.getLogger(ConfigurationBean.class);

    private String view;

    private String label1, label2, dimension1, dimension2;
    private static final List<ConfigurationService> configList = new ArrayList<>();

    private List<PieChartModel> chartList;
    private int columns;

    public FacesMessage msg = null;

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginbean;

    public LoginBean getLoginbean() {
        return loginbean;
    }

    public void setLoginbean(LoginBean loginbean) {
        this.loginbean = loginbean;
    }

    public List<ConfigurationService> getConfigList() {
        return configList;
    }

    public String getLabel1() {
        return label1;
    }

    public void setLabel1(String label1) {
        this.label1 = label1;
    }

    public String getLabel2() {
        return label2;
    }

    public void setLabel2(String label2) {
        this.label2 = label2;
    }

    public String getDimension1() {
        return dimension1;
    }

    public void setDimension1(String dimension1) {
        this.dimension1 = dimension1;
    }

    public String getDimension2() {
        return dimension2;
    }

    public void setDimension2(String dimension2) {
        this.dimension2 = dimension2;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public List<PieChartModel> getChartList() {
        return chartList;
    }

    public void setChartList(List<PieChartModel> chartList) {
        this.chartList = chartList;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    @PostConstruct
    public void init() {
        if (view == null || view.equals("")) {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            view = cleanURI(req.getHeader("Referer"));
        }
        columns = 1;
        genCharts();
    }

    public void add() {
        try {
            if (configList.size() < 3) {
                ConfigurationService con = new ConfigurationService(label1, label2, dimension1, dimension2);
                configList.add(con);

                label1 = "";
                label2 = "";
                dimension1 = "";
                dimension2 = "";

                msg = new FacesMessage("Item Added", label1);
                RequestContext.getCurrentInstance().execute("PF('addConfigDlg').hide()");
                columns = configList.size();
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "No more", null);
            }

            FacesContext.getCurrentInstance().addMessage(null, msg);
            genCharts();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void edit(RowEditEvent event) {
        try {
            msg = new FacesMessage("Item Edited", ((ConfigurationService) event.getObject()).getLabel1());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void cancel(RowEditEvent event) {
        try {
            msg = new FacesMessage("Item Cancelled");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            configList.remove((ConfigurationService) event.getObject());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void delete(ConfigurationService conf) {
        try {
            configList.remove(conf);
            msg = new FacesMessage("Item Deleted", conf.getLabel1());
            if (columns > 1) {
                columns = configList.size();
            }
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String cleanURI(String view) {
        String[] a = view.split("/");
        String b = toTitleCase(a[a.length - 1].replace(".xhtml", ""));

        return b;
    }

    public static String toTitleCase(String s) {
        String d = "";

        if (s.isEmpty()) {
            return s;
        }

        if (s.contains("_") || s.contains("-") || s.contains("/") || s.contains("\\.")) {
            String[] b = s.split("-|_|/|\\.");
            for (String c : b) {
                d += toTitleCase(c);
            }
        } else {
            d = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }

        return d;
    }

    public void genCharts() {
        if (!configList.isEmpty()) {

            chartList = new ArrayList<>();

            for (ConfigurationService cs : configList) {
                PieChartModel pieModel = new PieChartModel();

                pieModel.set(cs.getLabel1(), Math.random());
                pieModel.set(cs.getLabel2(), Math.random());

                pieModel.setTitle(cs.getDimension1());
                pieModel.setLegendPosition("w");

                chartList.add(pieModel);
            }
        }
    }

}
