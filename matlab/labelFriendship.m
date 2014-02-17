a = importdata('../../../dataset/Gowalla_edges.txt');
b = importdata('../frequent-pair.txt');

% get the most frequent pairs.
pair = b(:,1:2);
% get the friend label for the most frequent pairs.
[c, ia, ib] = intersect(a, pair, 'rows');
friendLabel = zeros(size(b,1), 1);
friendLabel(ib) = 1;


% import the different measure as features
% the 6 features are:
%   Renyi Diversity of co-locations
%   frequency weighted by location entropy
%   mutual information over complete location set
%   interestingness score from PAKDD
%   frequency
%   mutual entropy over co-location set
features = importdata('../feature-vectors.txt');
features = features';
nan_index = find(isnan(features));
features(nan_index) = 0;

fmean = repmat(min(features), size(features,1), 1);
fvariance = repmat(max(features-fmean), size(features,1), 1);
norm_features = (features - fmean) ./ fvariance;


mdl = LinearModel.fit(norm_features, friendLabel)
