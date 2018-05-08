package com.gretha.shared.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Filter implements Serializable {

	private static final long serialVersionUID = 1;

	private LocalDateTime ldtZeitraumVon;
	private LocalDateTime ldtZeitraumBis;
	private int status;
	private int parameter1;
	private int parameter2;
	private int bedingung1;
	private String wert1;
	private int logik;
	private int bedingung2;
	private String wert2;
	private String anzahlN;
	private boolean activated;
	private boolean letztenNVor;
	private boolean erstenNSeit;
	private int sortierung;

	public LocalDateTime getLdtZeitraumVon() {
		return ldtZeitraumVon;
	}

	public void setLdtZeitraumVon(LocalDateTime ldtZeitraumVon) {
		this.ldtZeitraumVon = ldtZeitraumVon;
	}

	public LocalDateTime getLdtZeitraumBis() {
		return ldtZeitraumBis;
	}

	public void setLdtZeitraumBis(LocalDateTime ldtZeitraumBis) {
		this.ldtZeitraumBis = ldtZeitraumBis;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getParameter1() {
		return parameter1;
	}

	public int getParameter2() {
		return parameter2;
	}

	public void setParameter1(int parameter1) {
		this.parameter1 = parameter1;
	}

	public void setParameter2(int parameter2) {
		this.parameter2 = parameter2;
	}

	public int getBedingung1() {
		return bedingung1;
	}

	public void setBedingung1(int bedingung1) {
		this.bedingung1 = bedingung1;
	}

	public String getWert1() {
		return wert1;
	}

	public void setWert1(String wert1) {
		this.wert1 = wert1;
	}

	public int getLogik() {
		return logik;
	}

	public void setLogik(int logik) {
		this.logik = logik;
	}

	public int getBedingung2() {
		return bedingung2;
	}

	public void setBedingung2(int bedingung2) {
		this.bedingung2 = bedingung2;
	}

	public String getWert2() {
		return wert2;
	}

	public void setWert2(String wert2) {
		this.wert2 = wert2;
	}

	public String getAnzahlN() {
		return anzahlN;
	}

	public void setAnzahlN(String anzahlN) {
		this.anzahlN = anzahlN;
	}

	public boolean isLetztenNVor() {
		return letztenNVor;
	}

	public void setLetztenNVor(boolean letztenNVor) {
		this.letztenNVor = letztenNVor;
	}

	public boolean isErstenNSeit() {
		return erstenNSeit;
	}

	public void setErstenNSeit(boolean erstenNSeit) {
		this.erstenNSeit = erstenNSeit;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public int getSortierung() {
		return sortierung;
	}

	public void setSortierung(int sortierung) {
		this.sortierung = sortierung;
	}

	@Override
	public String toString() {
		return "Filter [ldtZeitraumVon=" + ldtZeitraumVon + ", ldtZeitraumBis=" + ldtZeitraumBis + ", status=" + status
				+ ", parameter1=" + parameter1 + ", parameter2=" + parameter2 + ", bedingung1=" + bedingung1
				+ ", wert1=" + wert1 + ", logik=" + logik + ", bedingung2=" + bedingung2 + ", wert2=" + wert2
				+ ", anzahlN=" + anzahlN + ", activated=" + activated + ", letztenNVor=" + letztenNVor
				+ ", erstenNSeit=" + erstenNSeit + ", sortierung=" + sortierung + "]";
	}

}
