//
//  HomeViewModel.swift
//  iosApp
//
//  Created by Josue Lubaki on 2023-05-03.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

extension HomeScreen {
    @MainActor class HomeViewModel : ObservableObject {
        
        private let getMoviesUseCase = GetMoviesUseCase.init()
        
        @Published private(set) var movies : [Movie] = []
        @Published private(set) var isLoading : Bool = false
        
        private var currentPage : Int32 = 1
        private(set) var loadFinished : Bool = false
        
        func loadMovies() async {
            if isLoading {
                return
            }
            
            isLoading = true
            
            do {
                
                let movies = try await getMoviesUseCase.invoke(page: currentPage)
                isLoading = false
                loadFinished = movies.isEmpty
                
                currentPage += 1
                
                self.movies = self.movies + movies
                
            } catch {
                isLoading = false
                loadFinished = true
                
                print(error.localizedDescription)
            }
        }
    }
}
