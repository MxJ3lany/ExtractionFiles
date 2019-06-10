/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.transfer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import de.onyxbits.raccoon.gui.ButtonBarBuilder;
import de.onyxbits.raccoon.gui.DialogBuilder;
import de.onyxbits.raccoon.gui.Traits;
import de.onyxbits.weave.LifecycleManager;
import de.onyxbits.weave.swing.AbstractPanelBuilder;
import de.onyxbits.weave.swing.ActionLocalizer;

/**
 * 
 * The download dialog builder
 * 
 * @author patrick
 * 
 */
public class TransferViewBuilder extends AbstractPanelBuilder implements
		ActionListener {

	/**
	 * ID for referencing this builder.
	 */
	public static final String ID = TransferViewBuilder.class.getSimpleName();

	private static final int ITEMWIDTH = 360;

	protected JPanel list;
	private GridBagConstraints contentConstraints;
	protected JButton trim;
	protected JButton order;
	private Vector<TransferPeerBuilder> peers;

	@Override
	protected JPanel assemble() {
		peers = new Vector<TransferPeerBuilder>();
		GridBagConstraints spacerConstraints = new GridBagConstraints();
		spacerConstraints.gridx = GridBagConstraints.REMAINDER;
		spacerConstraints.gridy = GridBagConstraints.RELATIVE;
		spacerConstraints.weightx = 1;
		spacerConstraints.weighty = 1;
		spacerConstraints.fill = GridBagConstraints.BOTH;

		list = new JPanel();
		list.setLayout(new GridBagLayout());
		contentConstraints = new GridBagConstraints();
		contentConstraints.gridx = GridBagConstraints.REMAINDER;
		contentConstraints.gridy = GridBagConstraints.RELATIVE;
		contentConstraints.fill = GridBagConstraints.HORIZONTAL;
		contentConstraints.anchor = GridBagConstraints.NORTH;
		contentConstraints.insets.bottom = 3;

		ActionLocalizer al = Messages.getLocalizer();
		trim = new JButton(al.localize("trimdownloads"));
		trim.addActionListener(this);
		list.add(new JPanel(), spacerConstraints);
		
		
		JPanel cont = new JPanel();
		cont.setLayout(new GridBagLayout());
		GridBagConstraints contgbc = new GridBagConstraints();
		if (!globals.get(Traits.class).isAvailable("4.0.x")) {
			JTextArea txt = new JTextArea(Messages.getString(ID + ".booster.plug"));
			txt.setLineWrap(true);
			txt.setWrapStyleWord(true);
			txt.setPreferredSize(new Dimension(ITEMWIDTH + 2 * 10,50));
			txt.setEditable(false);
			txt.setBackground(null);
			txt.setForeground(Color.RED);
			cont.add(txt,contgbc);
			contgbc.gridx=0;
			contgbc.gridy =1;
			contgbc.insets = new Insets(5,5,5,5);
			contgbc.anchor=GridBagConstraints.NORTHEAST;
			order=new JButton(Messages.getString(ID + ".booster.ok"));
			order.addActionListener(this);
			cont.add(order,contgbc);
			contgbc.gridy=2;
			contgbc.gridx=0;
		}

		JScrollPane tmp = new JScrollPane(list);
		tmp.setBorder(BorderFactory.createEmptyBorder());
		tmp.getVerticalScrollBar().setUnitIncrement(20);
		tmp.setPreferredSize(new Dimension(ITEMWIDTH + 2 * 10, 400));
		tmp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		contgbc.weightx=1;
		contgbc.weighty=1;
		cont.add(tmp,contgbc);

		JPanel ret = new DialogBuilder(cont)
				.withTitle(Messages.getString(ID + ".title"))
				.withSubTitle(Messages.getString(ID + ".subtitle"))
				.withButtons(new ButtonBarBuilder().add(trim)).build(globals);

		globals.get(TransferManager.class).setPeer(this);
		return ret;
	}

	protected void add(TransferWorker tw) {
		TransferPeerBuilder peer = tw.getPeer();
		JPanel ctrl = peer.withBorder(BorderFactory.createEtchedBorder())
				.build(globals);
		Dimension d = ctrl.getPreferredSize();
		d.width = ITEMWIDTH;
		ctrl.setPreferredSize(d);
		// last item is always the spacer.
		list.add(ctrl, contentConstraints, list.getComponentCount() - 1);
		peers.add(peer);
		list.revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == trim) {
			for (int i=0;i<peers.size();i++) {
				list.getComponent(i).setVisible(peers.get(i).cancel.isEnabled());
			}
		}
		if (e.getSource() == order) {
			globals.get(LifecycleManager.class).sendBusMessage(new JTextField());
		}
	}
}
