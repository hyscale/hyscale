package main

import "fmt"
import "net/http"

func main(){
   http.HandleFunc("/",SimpleServer)
   http.ListenAndServe(":8001",nil)
}

func SimpleServer(w http.ResponseWriter, r *http.Request){
  fmt.Fprintf(w, "Hello, %s!", r.URL.Path[1:])
}
