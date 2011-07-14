package edu.stanford.bmir.protegex.icd.diff;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Map;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.ui.FrameRenderer;

public class ChangedFrameRenderer extends FrameRenderer {	

	private static final long serialVersionUID = 5646378888398296767L;

    private Map<Cls, FrameStatus> frame2status;

	private FrameStatus frameStatus;
	protected Cls _cls = null;

	private boolean _underline = false;
	private boolean _strikeOut = false;

	public ChangedFrameRenderer(Map<Cls, FrameStatus> frame2status) {
		this.frame2status = frame2status;
	}

	@Override
	protected void loadCls(Cls cls) {
		_cls = cls;
		super.loadCls(cls);
		frameStatus = frame2status.get(cls);

		if (frameStatus != null && frameStatus != FrameStatus.UNCHANGED) {
			setMainText(" " + frameStatus);
		}
		
		if (frameStatus == FrameStatus.ADDED) {
			setMainIcon(ICDIcons.getAddedIcon());
		} else if (frameStatus == FrameStatus.DELETED) {
			setMainIcon(ICDIcons.getDeletedIcon());	    	
		} else if (frameStatus == FrameStatus.CHILDREN_ADDED ||
				frameStatus == FrameStatus.CHILDREN_DELETED ||
				frameStatus == FrameStatus.CHILDREN_MOVED) {
			setMainIcon(ICDIcons.getClsWarningIcon());
		}
	}

	public Color getTextColor() {
		Color result;
		_underline = false;
		_strikeOut = false;

		if (frameStatus == FrameStatus.DELETED) {
			result = Color.red;
		} else if (frameStatus == FrameStatus.ADDED) {
			result = Color.blue;
		} else if (frameStatus == FrameStatus.MOVED) {
			result = Color.blue;
		} else if (frameStatus == FrameStatus.CHILDREN_ADDED) {
			result = Color.DARK_GRAY;
		} else if (frameStatus == FrameStatus.CHILDREN_MOVED) {
			result = Color.DARK_GRAY;
		} else if (frameStatus == FrameStatus.CHILDREN_DELETED) {
			result = Color.DARK_GRAY;
		} else if (frameStatus == FrameStatus.MOVED) {
			result = Color.blue;
		} else
			result = _foregroundNormalColor;

		if (result == null) {
			result = super.getTextColor();
		}

		return result;
	}


	public Font getFont () {
		Font result = super.getFont ();
		if (_cls == null) return result;

		if (frameStatus == FrameStatus.DELETED) {
			_strikeOut = true;			
			return result;
		} else if (frameStatus == FrameStatus.ADDED) {
			_underline = true;
			result = result.deriveFont(Font.BOLD);
			return result;
		} else if (frameStatus == FrameStatus.MOVED) {
			result = result.deriveFont(Font.BOLD);
			return result;
		} else if (frameStatus == FrameStatus.CHILDREN_ADDED ||
				frameStatus == FrameStatus.CHILDREN_DELETED ||
				frameStatus == FrameStatus.CHILDREN_MOVED) {
			result = result.deriveFont(Font.BOLD);
			return result;
		}	     

		return result;
	}


	protected void paintString(Graphics graphics, String text, Point position, Color color, Dimension size) {
		if (color != null) {
			graphics.setColor(color);
		}

		graphics.setFont(getFont());
		int y = (size.height + _fontMetrics.getAscent())/2 -2;	// -2 is a bizarre fudge factor that makes it look better!
		graphics.drawString(text, position.x, y);
		drawLine (graphics, position.x, (_fontMetrics.getHeight())/2, position.x + _fontMetrics.stringWidth(text), (_fontMetrics.getHeight())/2);
		position.x += _fontMetrics.stringWidth(text);
	}

	private void drawLine (Graphics g, int x1, int y1, int x2, int y2) { 
		if (_cls == null) return;
		if (_strikeOut)
			g.drawLine(x1, y1+1, x2, y2+1);
		if (_underline) {
			g.drawLine(x1, y1*2, x2, y2*2);
		}
	}


}
