import SwiftUI
import shared

struct HomeScreen: View {
    
    @StateObject var viewModel = HomeViewModel()
    
    let gridColumn : [GridItem] = Array(
        repeating: GridItem(
            .flexible(),
            spacing: 16
        ),
        count: 2
    )

	var body: some View {
        NavigationStack {
            
            ScrollView{
                
                LazyVGrid(columns: gridColumn, spacing: 16){
                    
                    ForEach(viewModel.movies, id: \.id){ movie in
                        MovieGridItem(movie: movie)
                            .task {
                                if movie == viewModel.movies.last
                                    && !viewModel.isLoading
                                    && !viewModel.loadFinished {
                                        await viewModel.loadMovies()
                                }
                            }
                    }
                    
                    if viewModel.isLoading {
                        Section(footer: ProgressView()){}
                    }
                    
                }
                .padding(.horizontal, 12)
                
            }
            .navigationTitle("Movies")
            
        }
        .task {
            await viewModel.loadMovies()
        }
	}
}

struct HomeScreen_Previews: PreviewProvider {
	static var previews: some View {
		HomeScreen()
	}
}
