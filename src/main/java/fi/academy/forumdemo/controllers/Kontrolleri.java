package fi.academy.forumdemo.controllers;

import fi.academy.forumdemo.entities.Alue;
import fi.academy.forumdemo.entities.Viesti;
import fi.academy.forumdemo.repositories.AlueRepository;
import fi.academy.forumdemo.repositories.ViestiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class Kontrolleri {
    private ViestiRepository vr;
    private AlueRepository ar;

    @Autowired
    public Kontrolleri(ViestiRepository vr, AlueRepository ar) {
        this.vr = vr;
        this.ar = ar;
    }

    @GetMapping("/alueet")
    public String alueet(Model model) {
        model.addAttribute("alueet", ar.findAll());
        return "alueet";
    }

    @GetMapping("/alue")
    public String alue(@RequestParam(name = "nimi") String nimi, Model model) {
        Optional<Alue> optAlue = ar.findById(nimi);
        if (optAlue.isPresent()) {
            Alue alue = optAlue.get();
            List<Viesti> langat = vr.haeViestitIlmanParenttia(alue);
            model.addAttribute("alue", alue);
            model.addAttribute("langat", langat);
            return "langat";
        }
        throw new RuntimeException("VIRHE");
    }

    @RequestMapping ("/uusilanka")
    public String luoUusiViestiKetjuLomake (@RequestParam(name="alue") @Valid String alue, Model model){
        Optional<Alue> optAlue = ar.findById(alue);
        if (optAlue.isPresent()){
            Alue hae = optAlue.get();
            Viesti uusiViesti = new Viesti();
            uusiViesti.setAlue(hae);
            model.addAttribute("uusiViesti", uusiViesti);
            return "uusilanka";

        }
        throw new RuntimeException("VIRHE");

    }

    @PostMapping ("/viestiketjut")
    public String uudenViestinLomakeKasittelija(Viesti uusiViesti, Model model) {
        vr.save(uusiViesti);
        return "redirect:naytaViestiketju?id=" + uusiViesti.getViesti_id();
    }

    @RequestMapping("/naytaViestiketju")
    public String viestiketju(@RequestParam(name = "id") int id, Model model) {
        //näyttää viestin ja viestin vastauksen, ei vielä vastausten vastauksia
        Optional<Viesti> optViesti = vr.findById(id);
        if (optViesti.isPresent()) {
            Viesti viesti = optViesti.get();
            while (viesti.getParent() != null) {
                viesti = viesti.getParent();
            }
            model.addAttribute("viesti", viesti);
            return "viestiketjut";
        }
        throw new RuntimeException("VIRHE");
    }

    @GetMapping("/vastaa")
    public String vastaa(@RequestParam(name = "id") int id, Model model) {
        Optional<Viesti> optViesti = vr.findById(id);
        if (optViesti.isPresent()) {
            Viesti mihinVastataan = optViesti.get();
            Viesti uusiViesti = new Viesti();
            uusiViesti.setParent(mihinVastataan); //asetetaan parent-kentän arvo
            uusiViesti.setAlue(mihinVastataan.getAlue()); //asetetaan alueeksi parentin alue
            model.addAttribute("uusiViesti", uusiViesti);
            return "kirjoitaVastaus";
        }
        throw new RuntimeException("VIRHE");
    }

    @PostMapping("/luoUusiVastaus")
    public String tallennavastaus(Viesti viesti, Model model) {
        vr.save(viesti);
        model.addAttribute("alueet", ar.findAll());
        return "redirect:naytaViestiketju?id=" + viesti.getViesti_id();
    }

    @GetMapping("/etusivu")
    public String uusimmatViestitEtusivulle(Model model) {
        List<Viesti> uudet = vr.ViestitAikajarjestyksessa();
        List<Viesti> limited = uudet.stream()
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("limited", limited);
        return "etusivu";
    }

    @PostMapping("/haku")
    public String haku(Hakusana hakusana, Model model) {
        List<Viesti> haetutViestit = vr.haeViestitHakusanalla(hakusana.getHakusana());
        List<Viesti> haetutViestitKirjoittaja = vr.haeKirjoittajanViestit(hakusana.getHakusana());
        model.addAttribute("haetutViestit", haetutViestit);
        model.addAttribute("haetutviestitkirjoittaja", haetutViestitKirjoittaja);
        return "haku";
    }

    @GetMapping("/nav")
    public String hakuKasittelija(Model model) {
        Hakusana hakusana = new Hakusana();
        model.addAttribute("hakusana", hakusana);
        return "nav";
    }


}


class Hakusana {
    private String hakusana;

    public String getHakusana() {
        return hakusana;
    }

    public void setHakusana(String hakusana) {
        this.hakusana = hakusana;
    }
}

