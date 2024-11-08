package ts.boundary;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import ts.entity.User;
import ts.boundary.mapping.Credential;
import ts.boundary.mapping.UserDTO;
import ts.store.UserStore;

@Path("users")
@Tag(name = "Gestione Users", description = "Permette di gestire gli utenti di bkmapp")
@PermitAll
public class UsersResources {
    
    @Inject
    private UserStore storeuser;
    
    @Context
    ResourceContext rc;
    
    @Context
    UriInfo uriInfo;
        
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Restituisce l'elenco di tutti gli utenti")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Elenco ritornato con successo"),
        @APIResponse(responseCode = "404", description = "Elenco non trovato")
    })
    public List<UserDTO> all() {
        List<UserDTO> usList = new ArrayList<>();
        storeuser.all().forEach(e -> {
           UserDTO us = new UserDTO();
            us.id = e.getId();
            us.username = e.getUsername(); // Aggiunto username
            us.name = e.getName();
            us.email = e.getEmail();
            us.pwd = ""; // Non restituire la password
          
           usList.add(us); // Aggiungi l'oggetto UserDTO alla lista
        });
        return usList;
    }
         
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Permette la registrazione di un nuovo utente")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Nuovo utente creato con successo"),
        @APIResponse(responseCode = "404", description = "Creazione di utente fallito")
    })
    @PermitAll
    public Response create(@Valid User entity) {
        
        if(storeuser.findUserbyLogin(entity.getEmail()).isPresent()){
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        
        if(entity.getPwd().length() < 4){
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        
        User saved = storeuser.save(entity);
        
        return Response.status(Response.Status.CREATED)
                .entity(saved)
                .build();
    }
    
    @POST
    @Path("login")
    @Operation(description = "Permette fare login e restituisce il token valido")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Login fatto con successo"),
        @APIResponse(responseCode = "404", description = "Login fallito")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public UserDTO login (@Valid Credential credential){
        User u = storeuser.login(credential)
                .orElseThrow(() -> new NotAuthorizedException("User non autorizzato",  
                                                                       Response.status(Response.Status.UNAUTHORIZED).build()));
        
        UserDTO us = new UserDTO();
        us.id = u.getId();
        us.username = u.getUsername(); // Aggiunto username
        us.name = u.getName();
        us.email = u.getEmail();
        us.pwd = ""; // Non restituire la password
       
        
        return us;
    }
    
    @DELETE
    @Path("{id}")
    @Operation(description = "Elimina una risorsa Utente tramite l'ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Utente eliminato con successo"),
        @APIResponse(responseCode = "404", description = "Utente non trovato")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response delete(@PathParam("id") Long id) {
        User found = storeuser.find(id)
                .orElseThrow(() -> new NotFoundException("User non trovato. ID=" + id));
        storeuser.remove(found);
        return Response.status(Response.Status.OK)
                .build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Aggiorna i dati dell'utente")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Utente aggiornato con successo"),
        @APIResponse(responseCode = "404", description = "Aggiornamento fallito")
    })
    @PermitAll
    public User update(@Valid User entity) {
        User found = storeuser.find(entity.getId())
                .orElseThrow(() -> new NotFoundException("User non trovato. ID=" + entity.getId()));
        return storeuser.update(entity);
    }
}
