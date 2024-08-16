package info.rsdev.playlists.rest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import info.rsdev.playlists.dao.ChartsItemDao;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.services.MusicChart;

@RestController
public class PlaylistFacade {

    private final ChartsItemDao dao;
    
    public PlaylistFacade(ChartsItemDao dao) {
        this.dao = dao;
    }
    
    @GetMapping(path = "/api/public/info")
    public InfoObject getInfo() {
        List<String> chartNames = Stream.of(MusicChart.values()).map(chart -> chart.chartName()).toList();
        return new InfoObject(chartNames);
    }
    
    @GetMapping(path = "/api/public/releases/{year}")
    public Collection<Song> getReleases(@PathVariable("year") short year) {
        return dao.getReleases(year);
    }

}
