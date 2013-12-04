/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.stl2developer;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.sabre.schemas.actions.ChangeAction;
import com.sabre.schemas.actions.CloseProjectAction;
import com.sabre.schemas.actions.CompileAction;
import com.sabre.schemas.actions.MergeNodesAction;
import com.sabre.schemas.actions.NewLibraryAction;
import com.sabre.schemas.actions.NewProjectAction;
import com.sabre.schemas.actions.OpenLibraryAction;
import com.sabre.schemas.actions.RemoveAllLibrariesAction;
import com.sabre.schemas.actions.RemoveLibrariesAction;
import com.sabre.schemas.actions.SaveSelectedLibraryAsAction;
import com.sabre.schemas.actions.ValidateAction;
import com.sabre.schemas.commands.SaveLibrariesHandler;
import com.sabre.schemas.commands.SaveLibraryHandler;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.utils.RCPUtils;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to
 * a workbench window. Each window will be populated with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction quitAction;
    private IWorkbenchAction about;
    private IAction validateAction;
    private IAction compileAction;

    private static IAction mergeAction;
    private static IAction closeProject;
    private static IAction closeLibrary;
    private static IAction closeAllLibraryInProjectes;

    public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {

        MainWindow mainWindow = OtmRegistry.getMainWindow();
        final MenuManager projectMenu = new MenuManager("Project", "Project");
        final MenuManager libraryMenu = new MenuManager("Library", "Library");
        final MenuManager editMenu = new MenuManager("Edit", "Edit");
        final MenuManager windowMenu = new MenuManager("Window", "Window");
        final MenuManager helpMenu = new MenuManager("Help", "Help");

        projectMenu.add(closeProject);
        NewProjectAction newProjectAction = new NewProjectAction();
        newProjectAction.setId("newProject");
        projectMenu.add(newProjectAction);
        projectMenu.add(compileAction);
        projectMenu.add(new Separator());
        projectMenu.add(quitAction);

        libraryMenu.add(new NewLibraryAction(mainWindow, new ExternalizedStringProperties(
                "action.new")));
        libraryMenu.add(new OpenLibraryAction());

        libraryMenu.add(validateAction);
        libraryMenu.add(new Separator());
        IContributionItem saveSelectedLibrary = RCPUtils.createCommandContributionItem(PlatformUI
                .getWorkbench(), SaveLibraryHandler.COMMAND_ID, null, null, Images
                .getImageRegistry().getDescriptor(Images.Save));
        libraryMenu.add(saveSelectedLibrary);
        IContributionItem saveAll = RCPUtils.createCommandContributionItem(PlatformUI
                .getWorkbench(), SaveLibrariesHandler.COMMAND_ID, null, null, Images
                .getImageRegistry().getDescriptor(Images.SaveAll));
        libraryMenu.add(saveAll);
        libraryMenu.add(new SaveSelectedLibraryAsAction(mainWindow,
                new ExternalizedStringProperties("action.saveSelectedAs")));
        libraryMenu.add(new Separator());
        libraryMenu.add(closeLibrary);
        libraryMenu.add(closeAllLibraryInProjectes);

        editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
        editMenu.add(new ChangeAction(mainWindow, new ExternalizedStringProperties(
                "action.changeObject")));
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));

        helpMenu.add(about);

        menuBar.add(projectMenu);
        menuBar.add(libraryMenu);
        menuBar.add(editMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);

    }

    @Override
    protected void makeActions(IWorkbenchWindow window) {
        IWorkbenchAction deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);
        quitAction = ActionFactory.QUIT.create(window);
        register(quitAction);
        register(ActionFactory.SHOW_EDITOR.create(window));
        closeProject = new CloseProjectAction();
        register(closeProject);
        closeLibrary = new RemoveLibrariesAction();
        register(closeLibrary);
        closeAllLibraryInProjectes = new RemoveAllLibrariesAction();
        register(closeAllLibraryInProjectes);
        mergeAction = new MergeNodesAction();
        register(mergeAction);
        about = ActionFactory.ABOUT.create(window);
        register(about);
        compileAction = new CompileAction();
        register(compileAction);
        validateAction = new ValidateAction();
        register(validateAction);

    }

    public IAction getMergeAction() {
        return mergeAction;
    }

    public static IAction getCloseProject() {
        return closeProject;
    }

    public static IAction getCloseLibrary() {
        return closeLibrary;
    }

    public static IAction getCloseAllLibraryInProjectes() {
        return closeAllLibraryInProjectes;
    }
}