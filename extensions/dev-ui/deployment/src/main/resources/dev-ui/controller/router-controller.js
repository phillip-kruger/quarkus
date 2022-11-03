import { Router } from '@vaadin/router';
// TODO: Some Method can be static
export class RouterController {
    static router = new Router(document.querySelector('qwc-page'));
    static pathContext = new Map();

    host;

    constructor(host) {
        (this.host = host).addController(this);

        window.addEventListener('vaadin-router-location-changed', (event) => {
            var component = event.detail.location.route.component;
            var path = event.detail.location.route.path;
            var name = event.detail.location.route.name;

            var display = this.getPageDisplayName(component);
            const switchPageEvent = new CustomEvent('switchPage',
                    {
                        detail: {
                            component: component,
                            path: path,
                            display: display,
                            displayName: name
                        }
                    });
            dispatchEvent(switchPageEvent);
        });
    }

    getPageDisplayName(pageName) {
        pageName = pageName.substring(pageName.indexOf('-') + 1);
        pageName = pageName.charAt(0).toUpperCase() + pageName.slice(1);
        return pageName.replaceAll('-', ' ');
    }

    getBasePath() {
        var base = window.location.pathname;
        return base.substring(0, base.indexOf('/dev')) + "/dev-ui";
    }

    getPageRef(pageName) {
        return pageName.substring(pageName.indexOf('-') + 1);
    }

    pathExist(path) {
        if (RouterController.pathContext && RouterController.pathContext.size > 0 && RouterController.pathContext.has(path)) {
            return true;
        }
        return false;
    }

    getCurrentMetaData() {
        var location = RouterController.router.location;
        if (location.route) {
            var currentRoutePath = location.route.path;
            return this.getMetaDataForPath(currentRoutePath);
        }else{
            return null;
        }
    }

    getMetaDataForPath(path) {
        if (this.pathExist(path)) {
            return RouterController.pathContext.get(path);
        }else{
            return null;
        }
    }

    getCurrentTitle(){
        var location = RouterController.router.location;
        if (location.route) {
            var currentRoutePath = location.route.path;
            return this.getTitleForPath(currentRoutePath);
        }else{
            return null;
        }
    }

    getTitleForPath(path){
        if(path.includes('/dev-ui/')){
            var metadata = this.getMetaDataForPath(path);
            if(metadata && metadata.extensionName){
                return metadata.extensionName;
            }else{
                var currentPage = path.substring(path.indexOf('/dev-ui/') + 8);
                if(currentPage.includes('/')){
                    // This is a submenu
                    var extension = currentPage.substring(0, currentPage.lastIndexOf("/"));
                    return this.formatTitle(extension);
                }else{
                    // This is a main section
                    return this.formatTitle(currentPage);
                }
            }
        }
        return "";
    }

    getCurrentSubMenu(){
        var location = RouterController.router.location;
        if (location.route) {
            var currentRoutePath = location.route.path;
            return this.getSubMenuForPath(currentRoutePath);
        }else{
            return null;
        }
    }

    getSubMenuForPath(path){
        
        if(path.includes('/dev-ui/')){
            var currentPage = path.substring(path.indexOf('/dev-ui/') + 8);
            if(currentPage.includes('/')){
                // This is a submenu
                const links = [];
                var startOfPath = path.substring(0, path.lastIndexOf("/"));
                var routes = RouterController.router.getRoutes();

                var counter = 0;
                var index = 0;
                routes.forEach((route) => {
                    if(route.path.startsWith(startOfPath)){
                        links.push(route);
                        if(route.name === RouterController.router.location.route.name){
                            index = counter;
                        }
                        counter = counter + 1;
                    }
                });

                if (links && links.length > 1) {
                    return {
                        'index': index,
                        'links': links
                    };
                }
            }
        }
        return null;
    }

    formatTitle(title) {
        title = title.charAt(0).toUpperCase() + title.slice(1);
        return title.split("-").join(" ");
    }

    /**
     * Add a route to the routes
     */
    addRoute(path, component, name, metadata, defaultRoute = false) {
        var base = this.getBasePath();
        path = base + '/' + path;

        if (!this.pathExist(path)) {
            RouterController.pathContext.set(path, metadata);
            var routes = [];
            var route = {};
            route.path = path;
            route.component = component;
            route.name = name;

            routes.push({...route});

            RouterController.router.addRoutes(routes);
        }
        // TODO: Pass the other parameters along ?
        var currentSelection = window.location.pathname;

        var relocationRequest = this.getFromQueryParameter();
        if (relocationRequest) {
            // We know and already loaded the requested location
            if (relocationRequest === path) {
                Router.go({pathname: path});
            }
        } else {
            // We know and already loaded the requested location
            if (currentSelection === path) {
                Router.go({pathname: path});
                // The default naked route  
            } else if (!RouterController.router.location.route && defaultRoute && currentSelection.endsWith('/dev-ui/')) {
                Router.go({pathname: path});
                // We do not know and have not yet loaded the requested location
            } else if (!RouterController.router.location.route && defaultRoute) {
                Router.go({
                    pathname: path,
                    search: '?from=' + currentSelection,
                });
            }
    }
    }

    getFromQueryParameter() {
        const params = new Proxy(new URLSearchParams(window.location.search), {
            get: (searchParams, prop) => searchParams.get(prop),
        });

        return params.from;
    }

}