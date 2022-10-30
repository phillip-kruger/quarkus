import { Router } from '@vaadin/router';

export class RouterController {
  static router = new Router(document.querySelector('qwc-page'));
  static addedPaths = [];
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
          path : path,
          display : display,
          displayName: name
        }
      });
      dispatchEvent(switchPageEvent);
    });
  }

  getPageDisplayName(pageName){
    pageName = pageName.substring(pageName.indexOf('-') + 1); 
    pageName = pageName.charAt(0).toUpperCase() + pageName.slice(1);
    return pageName.replaceAll('-', ' ');
  }

  getBasePath(){
    var base = window.location.pathname;
    return base.substring(0, base.indexOf('/dev')) + "/dev-ui";
  }

  getPageRef(pageName){
    return pageName.substring(pageName.indexOf('-') + 1); 
  }

  /**
   * Add a route to the routes
   */
  addRoute(path, component, displayName = null, defaultRoute = false){
    var base = this.getBasePath();
    path = base + '/' + path;
    
    if(!displayName){
        displayName = this.getPageDisplayName(component);
    }
    
    if(!RouterController.addedPaths.includes(path)){
        RouterController.addedPaths.push(path);
        var routes = [];
        var route = {};
        route.path = path;
        route.component = component;
        route.name = displayName;
        routes.push({...route});
        
        RouterController.router.addRoutes(routes);
    }
    // TODO: Pass the other parameters along ?
    var currentSelection = window.location.pathname;

    var relocationRequest = this.getFromQueryParameter();
    if(relocationRequest){
      // We know and already loaded the requested location
      if(relocationRequest === path){
        Router.go({pathname: path});
      }
    }else{
      // We know and already loaded the requested location
      if(currentSelection === path){
        Router.go({pathname: path});
      // The default naked route  
      }else if (!RouterController.router.location.route && defaultRoute && currentSelection.endsWith('/dev-ui/')){
        Router.go({pathname: path});
      // We do not know and have not yet loaded the requested location
      }else if (!RouterController.router.location.route && defaultRoute){
        Router.go({
          pathname: path,
          search: '?from=' + currentSelection,
        });
      }
    }
  }

  getFromQueryParameter(){
    const params = new Proxy(new URLSearchParams(window.location.search), {
      get: (searchParams, prop) => searchParams.get(prop),
    });
    
    return params.from;
  }
  
}