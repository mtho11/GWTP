/**
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtplatform.mvp.client;

import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.LockInteractionHandler;
import com.gwtplatform.mvp.client.proxy.ResetPresentersEvent;
import com.gwtplatform.mvp.client.proxy.ResetPresentersHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealRootContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentHandler;

/**
 * This is the presenter for the top-level of the application. It is derived
 * from presenter widget, but it's just because it doesn't need a proxy has it
 * will be bound as an eager singleton. It sets content within GWT's
 * {@link RootPanel} and {@link RootLayoutPanel}.
 * <p/>
 * Fire a {@link RevealRootContentEvent} or {@link RevealRootLayoutContentEvent}
 * to set your presenter at the top level. The choice depends on whether your
 * presenter works as a {@link com.google.gwt.user.client.ui.Panel} or as a
 * {@link com.google.gwt.user.client.ui.LayoutPanel}.
 *
 * @author Philippe Beaudoin
 */
public class RootPresenter extends PresenterWidget<RootPresenter.RootView>
        implements ResetPresentersHandler, RevealRootContentHandler,
        RevealRootLayoutContentHandler, RevealRootPopupContentHandler,
        LockInteractionHandler {

    /**
     * {@link RootPresenter}'s view.
     */
    public static class RootView extends ViewImpl {

        private boolean usingRootLayoutPanel;

        /**
         * The glass element.
         */
        private Element glass;

        @Override
        public Widget asWidget() {
            assert false : "Root view has no widget, you should never call asWidget()";
            return null;
        }

        @Override
        public void setInSlot(Object slot, IsWidget content) {
            assert slot == rootSlot : "Unknown slot used in the root proxy.";

            if (usingRootLayoutPanel) {
                // TODO Next 3 lines are a dirty workaround for
                // http://code.google.com/p/google-web-toolkit/issues/detail?id=4737
                RootPanel.get().clear();
                RootLayoutPanel.get().clear();
                RootPanel.get().add(RootLayoutPanel.get());
                if (content != null) {
                    RootLayoutPanel.get().add(content);
                }
            } else {
                // TODO Next 2 lines are a dirty workaround for
                // http://code.google.com/p/google-web-toolkit/issues/detail?id=4737
                RootLayoutPanel.get().clear();
                RootPanel.get().clear();
                if (content != null) {
                    RootPanel.get().add(content);
                }
            }
        }

        private void setUsingRootLayoutPanel(boolean usingRootLayoutPanel) {
            this.usingRootLayoutPanel = usingRootLayoutPanel;
        }

        public void lockScreen() {
            ensureGlass();
            Document.get().getBody().appendChild(glass);
        }

        public void unlockScreen() {
            ensureGlass();
            Document.get().getBody().removeChild(glass);
        }

        public void ensureGlass() {
            if (glass == null) {
                glass = Document.get().createDivElement();

                Style style = glass.getStyle();
                style.setPosition(Position.ABSOLUTE);
                style.setLeft(0, Unit.PX);
                style.setTop(0, Unit.PX);
                style.setRight(0, Unit.PX);
                style.setBottom(0, Unit.PX);
                style.setZIndex(2147483647); // Maximum z-index
            }
        }
    }

    private static final Object rootSlot = new Object();

    private boolean isResetting;

    /**
     * Creates a proxy class for a presenter that can contain tabs.
     * 
     * @param eventBus
     *            The event bus.
     */
    @Inject
    public RootPresenter(final EventBus eventBus, final RootView view) {
        super(eventBus, view);
        visible = true;
    }

    @Override
    protected void onBind() {
        super.onBind();

        addRegisteredHandler(ResetPresentersEvent.getType(), this);

        addRegisteredHandler(RevealRootContentEvent.getType(), this);

        addRegisteredHandler(RevealRootLayoutContentEvent.getType(), this);

        addRegisteredHandler(RevealRootPopupContentEvent.getType(), this);

        addRegisteredHandler(LockInteractionEvent.getType(), this);
    }

    @Override
    public void onResetPresenters(ResetPresentersEvent resetPresentersEvent) {
        if (!isResetting) {
            isResetting = true;
            internalReset();
            isResetting = false;
        }
    }

    @Override
    public void onRevealRootContent(
            final RevealRootContentEvent revealContentEvent) {
        getView().setUsingRootLayoutPanel(false);
        setInSlot(rootSlot, revealContentEvent.getContent());
    }

    public void onRevealRootLayoutContent(
            final RevealRootLayoutContentEvent revealContentEvent) {
        getView().setUsingRootLayoutPanel(true);
        setInSlot(rootSlot, revealContentEvent.getContent());
    }

    @Override
    public void onRevealRootPopupContent(
            final RevealRootPopupContentEvent revealContentEvent) {
        if (revealContentEvent.isCentered()) {
            addToPopupSlot(revealContentEvent.getContent());
        } else {
            addToPopupSlot(revealContentEvent.getContent(), false);
        }
    }

    @Override
    public void onLockInteraction(LockInteractionEvent lockInteractionEvent) {
        if (lockInteractionEvent.shouldLock()) {
            getView().lockScreen();
        } else {
            getView().unlockScreen();
        }
    }

}
